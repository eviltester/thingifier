package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

final class DeleteThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;

    DeleteThingHandler(final ThingStore store, final ThingDefinitionResolver definitions) {
        this.store = store;
        this.definitions = definitions;
    }

    ThingCommandResult handle(final DeleteThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

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

    ThingCommandResult apply(final DeleteThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }
        try {
            store.entities().delete(instance);
            return ThingCommandResult.success();
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }
}
