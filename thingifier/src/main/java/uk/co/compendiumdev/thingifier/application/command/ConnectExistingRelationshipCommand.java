package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class ConnectExistingRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final String relationshipName;
    private final EntityInstance child;

    public ConnectExistingRelationshipCommand(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstance child) {
        this.parent = parent;
        this.relationshipName = relationshipName;
        this.child = child;
    }

    public EntityInstance getParent() {
        return parent;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public EntityInstance getChild() {
        return child;
    }
}
