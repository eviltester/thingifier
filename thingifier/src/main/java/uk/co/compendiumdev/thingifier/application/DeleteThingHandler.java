package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

/**
 * Validates and applies commands that delete entity instances.
 *
 * <p>Validation only proves the target exists; apply performs the actual store mutation.
 */
final class DeleteThingHandler {

    private final ThingDefinitionResolver definitions;
    private final RelationshipCascadeDeleteService cascadeDeletes;

    /**
     * Creates the delete handler.
     *
     * @param store store to mutate
     * @param definitions resolver for model definitions and instances
     */
    DeleteThingHandler(final ThingStore store, final ThingDefinitionResolver definitions) {
        this(store, definitions, new RelationshipCascadeDeleteService(store));
    }

    /**
     * Creates the delete handler with an explicit cascade delete service for focused tests.
     *
     * @param store store associated with the command service
     * @param definitions resolver for model definitions and instances
     * @param cascadeDeletes service that applies configured relationship ownership deletes
     */
    DeleteThingHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final RelationshipCascadeDeleteService cascadeDeletes) {
        this.definitions = definitions;
        this.cascadeDeletes = cascadeDeletes;
    }

    /**
     * Validates and applies a delete command in one call.
     *
     * @param command delete command to handle
     * @return validation error or successful delete result
     */
    ThingCommandResult handle(final DeleteThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates a delete command without mutating the store.
     *
     * @param command delete command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final DeleteThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }
        return null;
    }

    /**
     * Applies a delete command after validation.
     *
     * @param command validated delete command
     * @return successful command result or store error
     */
    ThingCommandResult apply(final DeleteThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }
        try {
            cascadeDeletes.deleteInstance(instance);
            return ThingCommandResult.success();
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }
}
