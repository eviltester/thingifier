package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class InstanceRoute extends ThingRoute {

    private final EntityDefinition entity;
    private final String identifier;

    InstanceRoute(
            final String originalPath, final EntityDefinition entity, final String identifier) {
        super(originalPath);
        this.entity = entity;
        this.identifier = identifier;
    }

    public EntityDefinition entity() {
        return entity;
    }

    public String identifier() {
        return identifier;
    }
}
