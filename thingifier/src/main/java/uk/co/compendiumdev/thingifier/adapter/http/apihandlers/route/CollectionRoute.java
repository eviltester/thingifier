package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class CollectionRoute extends ThingRoute {

    private final EntityDefinition entity;

    CollectionRoute(final String originalPath, final EntityDefinition entity) {
        super(originalPath);
        this.entity = entity;
    }

    public EntityDefinition entity() {
        return entity;
    }
}
