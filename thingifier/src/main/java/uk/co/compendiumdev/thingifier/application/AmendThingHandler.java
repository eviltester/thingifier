package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class AmendThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final CreateThingHandler createHandler;
    private final RelationshipConnectionService relationships;

    AmendThingHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final WriteValidationPolicy validation,
            final ThingDraftFactory drafts,
            final CreateThingHandler createHandler,
            final RelationshipConnectionService relationships) {
        this.store = store;
        this.definitions = definitions;
        this.validation = validation;
        this.drafts = drafts;
        this.createHandler = createHandler;
        this.relationships = relationships;
    }

    ThingCommandResult handle(final AmendThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }

        try {
            List<NamedValue> fieldValues =
                    validation.normalizedFieldValues(
                            entity, command.getFieldValues(), command.getBodyFields());
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(instance).setFieldValuesFrom(fieldValues);
            return amend(
                    instance,
                    draft,
                    command.shouldReplaceExistingFieldsAndRelationships(),
                    command.getRelationships());
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    ThingCommandResult handle(final ReplaceThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance != null) {
            try {
                EntityInstanceDraft draft =
                        new EntityInstanceDraftBuilder(instance).setFieldValuesFrom(fieldValues);
                return amend(instance, draft, true, command.getRelationships());
            } catch (Exception e) {
                return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
            }
        }

        ThingCommandResult creationAllowed =
                validation.validateReplaceCreate(entity, command.getIdentifier(), fieldValues);
        if (creationAllowed != null) {
            return creationAllowed;
        }

        try {
            EntityInstanceDraft draft =
                    drafts.createDraft(entity, command.getIdentifier(), fieldValues);
            ThingCommandResult created =
                    createHandler.create(draft, command.getRelationships(), true);
            if (created.isError()) {
                return created;
            }
            return ThingCommandResult.created(created.getInstance());
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    private ThingCommandResult amend(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationshipReferences) {
        RelationshipSnapshot originalRelationships = RelationshipSnapshot.capture(store, instance);
        try {
            EntityInstance updated;
            if (replaceExistingFieldsAndRelationships) {
                updated = store.entities().replace(instance, draft);
                originalRelationships.disconnectFrom(store, updated);
            } else {
                updated = store.entities().patch(instance, draft);
            }

            ThingCommandResult relationshipResult =
                    relationships.connectRelationshipReferences(
                            updated, relationshipReferences, true, false);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }

            if (replaceExistingFieldsAndRelationships) {
                originalRelationships.deleteFormerDependentsMadeInvalidBy(store, updated);
            }

            return ThingCommandResult.success(updated);
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }
}
