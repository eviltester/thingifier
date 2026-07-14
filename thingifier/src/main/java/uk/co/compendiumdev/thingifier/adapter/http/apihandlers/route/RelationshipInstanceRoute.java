package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class RelationshipInstanceRoute extends ThingRoute {

    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    RelationshipInstanceRoute(
            final String originalPath,
            final EntityDefinition parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier) {
        super(originalPath);
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childIdentifier = childIdentifier;
    }

    public EntityDefinition parentEntity() {
        return parentEntity;
    }

    public String parentIdentifier() {
        return parentIdentifier;
    }

    public String relationshipName() {
        return relationshipName;
    }

    public String childIdentifier() {
        return childIdentifier;
    }
}
