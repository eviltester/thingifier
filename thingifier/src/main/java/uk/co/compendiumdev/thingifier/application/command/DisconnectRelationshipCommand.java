package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class DisconnectRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final EntityInstance child;
    private final String relationshipName;

    public DisconnectRelationshipCommand(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        this.parent = parent;
        this.child = child;
        this.relationshipName = relationshipName;
    }

    public EntityInstance getParent() {
        return parent;
    }

    public EntityInstance getChild() {
        return child;
    }

    public String getRelationshipName() {
        return relationshipName;
    }
}
