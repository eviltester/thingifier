package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class RelationshipConnection {

    private final String relationshipName;
    private final EntityInstance relatedInstance;

    public RelationshipConnection(
            final String relationshipName, final EntityInstance relatedInstance) {
        this.relationshipName = relationshipName;
        this.relatedInstance = relatedInstance;
    }

    public String relationshipName() {
        return relationshipName;
    }

    public EntityInstance relatedInstance() {
        return relatedInstance;
    }
}
