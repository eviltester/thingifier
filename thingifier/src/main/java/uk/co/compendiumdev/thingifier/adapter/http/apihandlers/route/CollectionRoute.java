package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

public final class CollectionRoute extends ThingRoute {

    private final EntityTypeRef entity;

    CollectionRoute(final String originalPath, final EntityTypeRef entity) {
        super(originalPath);
        this.entity = entity;
    }

    public EntityTypeRef entity() {
        return entity;
    }
}
