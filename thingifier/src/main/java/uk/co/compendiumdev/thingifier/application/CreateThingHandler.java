package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

final class CreateThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final RelationshipConnectionService relationships;

    CreateThingHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final WriteValidationPolicy validation,
            final ThingDraftFactory drafts,
            final RelationshipConnectionService relationships) {
        this.store = store;
        this.definitions = definitions;
        this.validation = validation;
        this.drafts = drafts;
        this.relationships = relationships;
    }

    ThingCommandResult handle(final CreateThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        if (entity == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format("Could not find entity %s", command.getEntityName())));
        }

        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypes(entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        ThingCommandResult validationResult =
                validation.validateCreate(
                        entity,
                        fieldValues,
                        command.hasRequestedPrimaryKey(),
                        command.getRequestedPrimaryKey());
        if (validationResult != null) {
            return validationResult;
        }

        try {
            EntityInstanceDraft draft =
                    drafts.createDraft(entity, command.getRequestedPrimaryKey(), fieldValues);
            return create(
                    draft, command.getRelationships(), command.shouldValidateFinalRelationships());
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    ThingCommandResult create(
            final EntityInstanceDraft draft,
            final List<RelationshipReference> relationshipReferences,
            final boolean validateFinalRelationships) {
        try {
            EntityInstance created = store.entities().create(draft);
            ThingCommandResult relationshipResult =
                    relationships.connectRelationshipReferences(
                            created, relationshipReferences, validateFinalRelationships, true);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }
            return ThingCommandResult.success(created);
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }
}
