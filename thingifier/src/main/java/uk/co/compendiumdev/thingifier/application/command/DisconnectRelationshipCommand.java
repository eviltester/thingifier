package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class DisconnectRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final EntityInstance child;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;
    private final String routeDisplay;

    public DisconnectRelationshipCommand(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        this.parent = parent;
        this.child = child;
        this.parentEntity = parent.getEntity();
        this.parentIdentifier = parent.getPrimaryKeyValue();
        this.relationshipName = relationshipName;
        this.childIdentifier = child.getPrimaryKeyValue();
        this.routeDisplay = "";
    }

    public DisconnectRelationshipCommand(
            final EntityDefinition parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier,
            final String routeDisplay) {
        this.parent = null;
        this.child = null;
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childIdentifier = childIdentifier;
        this.routeDisplay = routeDisplay;
    }

    public EntityInstance getParent() {
        return parent;
    }

    public EntityInstance getChild() {
        return child;
    }

    public boolean hasResolvedRelationship() {
        return parent != null && child != null;
    }

    public EntityDefinition getParentEntity() {
        return parentEntity;
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
