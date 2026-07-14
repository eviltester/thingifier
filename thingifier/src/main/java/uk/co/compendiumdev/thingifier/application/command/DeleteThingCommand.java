package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class DeleteThingCommand implements ThingWriteCommand {

    private final EntityInstance instance;
    private final EntityDefinition entity;
    private final String identifier;
    private final String routeDisplay;

    public DeleteThingCommand(final EntityInstance instance) {
        this.instance = instance;
        this.entity = instance.getEntity();
        this.identifier = instance.getPrimaryKeyValue();
        this.routeDisplay = "";
    }

    public DeleteThingCommand(
            final EntityDefinition entity, final String identifier, final String routeDisplay) {
        this.instance = null;
        this.entity = entity;
        this.identifier = identifier;
        this.routeDisplay = routeDisplay;
    }

    public EntityInstance getInstance() {
        return instance;
    }

    public boolean hasResolvedInstance() {
        return instance != null;
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}
