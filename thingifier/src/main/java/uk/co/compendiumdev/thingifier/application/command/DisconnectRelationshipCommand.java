package uk.co.compendiumdev.thingifier.application.command;

public final class DisconnectRelationshipCommand implements ThingWriteCommand {

    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;
    private final String routeDisplay;

    public DisconnectRelationshipCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier,
            final String routeDisplay) {
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childIdentifier = childIdentifier;
        this.routeDisplay = routeDisplay == null ? "" : routeDisplay;
    }

    public String getParentEntityName() {
        return parentEntityName;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public String getChildIdentifier() {
        return childIdentifier;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}
