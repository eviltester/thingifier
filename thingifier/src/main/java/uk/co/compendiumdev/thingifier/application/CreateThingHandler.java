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

/**
 * Validates and applies commands that create entity instances.
 *
 * <p>The handler keeps validation separate from mutation so lifecycle hooks can inspect or replace
 * validation results before the store is changed.
 */
final class CreateThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final RelationshipConnectionService relationships;

    /**
     * Creates the entity creation handler.
     *
     * @param store store to mutate
     * @param definitions resolver for model definitions and instances
     * @param validation write validation policy
     * @param drafts factory for validated entity drafts
     * @param relationships service used to connect relationship references after creation
     */
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

    /**
     * Validates and applies a create command in one call.
     *
     * @param command create command to handle
     * @return validation error or successful create result
     */
    ThingCommandResult handle(final CreateThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates a create command without mutating the store.
     *
     * @param command create command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final CreateThingCommand command) {
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
        return null;
    }

    /**
     * Applies a create command after validation.
     *
     * @param command validated create command
     * @return command result containing the created instance or an error
     */
    ThingCommandResult apply(final CreateThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        if (entity == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format("Could not find entity %s", command.getEntityName())));
        }
        try {
            List<NamedValue> fieldValues =
                    validation.normalizedFieldValues(
                            entity, command.getFieldValues(), command.getBodyFields());
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

    /**
     * Persists a draft and connects any relationship references supplied with the create command.
     *
     * @param draft draft entity to persist
     * @param relationshipReferences relationships to connect after creation
     * @param validateFinalRelationships true when final relationship constraints should be checked
     * @return command result containing the created instance or an error
     */
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
