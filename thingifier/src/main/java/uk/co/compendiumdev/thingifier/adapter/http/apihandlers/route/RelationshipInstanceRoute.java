package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

public final class RelationshipInstanceRoute extends ThingRoute {

    private final EntityTypeRef parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    RelationshipInstanceRoute(
            final String originalPath,
            final EntityTypeRef parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier) {
        super(originalPath);
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childIdentifier = childIdentifier;
    }

    public EntityTypeRef parentEntity() {
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
