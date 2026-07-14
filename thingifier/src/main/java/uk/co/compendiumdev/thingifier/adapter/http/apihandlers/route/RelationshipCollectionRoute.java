package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

public final class RelationshipCollectionRoute extends ThingRoute {

    private final EntityTypeRef parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;

    RelationshipCollectionRoute(
            final String originalPath,
            final EntityTypeRef parentEntity,
            final String parentIdentifier,
            final String relationshipName) {
        super(originalPath);
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
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
}
