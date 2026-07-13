package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class DeleteThingCommand implements ThingWriteCommand {

    private final EntityInstance instance;

    public DeleteThingCommand(final EntityInstance instance) {
        this.instance = instance;
    }

    public EntityInstance getInstance() {
        return instance;
    }
}
