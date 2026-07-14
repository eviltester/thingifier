package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class DeleteThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;

    DeleteThingHandler(final ThingStore store, final ThingDefinitionResolver definitions) {
        this.store = store;
        this.definitions = definitions;
    }

    ThingCommandResult handle(final DeleteThingCommand command) {
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
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }
}
