package uk.co.compendiumdev.thingifier.application.command;

public final class DeleteThingCommand implements ThingWriteCommand {

    private final String entityName;
    private final String identifier;
    private final String routeDisplay;

    public DeleteThingCommand(
            final String entityName, final String identifier, final String routeDisplay) {
        this.entityName = entityName;
        this.identifier = identifier;
        this.routeDisplay = routeDisplay == null ? "" : routeDisplay;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}
