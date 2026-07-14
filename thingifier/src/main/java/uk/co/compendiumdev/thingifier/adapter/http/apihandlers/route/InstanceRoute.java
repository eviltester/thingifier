package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

public final class InstanceRoute extends ThingRoute {

    private final EntityTypeRef entity;
    private final String identifier;

    InstanceRoute(final String originalPath, final EntityTypeRef entity, final String identifier) {
        super(originalPath);
        this.entity = entity;
        this.identifier = identifier;
    }

    public EntityTypeRef entity() {
        return entity;
    }

    public String identifier() {
        return identifier;
    }
}
