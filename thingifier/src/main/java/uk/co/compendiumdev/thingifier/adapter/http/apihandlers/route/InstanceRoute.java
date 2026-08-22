package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

/**
 * Route mapping for a request that targets one entity instance.
 *
 * <p>Generated routes obtain the identifier from the URL. Fixed-instance routes obtain the
 * identifier from API-spec configuration, so handlers need to know when the identifier was trusted
 * configuration rather than a path segment that might really be another route.
 */
public final class InstanceRoute extends ThingRoute {

    private final EntityTypeRef entity;
    private final String identifier;
    private final boolean fixedIdentifier;

    InstanceRoute(final String originalPath, final EntityTypeRef entity, final String identifier) {
        this(originalPath, entity, identifier, false);
    }

    InstanceRoute(
            final String originalPath,
            final EntityTypeRef entity,
            final String identifier,
            final boolean fixedIdentifier) {
        super(originalPath);
        this.entity = entity;
        this.identifier = identifier;
        this.fixedIdentifier = fixedIdentifier;
    }

    /**
     * Returns the entity whose instance is targeted.
     *
     * @return entity type reference
     */
    public EntityTypeRef entity() {
        return entity;
    }

    /**
     * Returns the instance identifier to use for reads and writes.
     *
     * @return route or configured identifier
     */
    public String identifier() {
        return identifier;
    }

    /**
     * Reports whether the identifier came from fixed-route configuration.
     *
     * <p>Generated route reads still apply route-shape ambiguity checks, but fixed identifiers are
     * already an explicit application decision and should be used as supplied.
     *
     * @return true when the identifier was configured with {@code withFixedIdentifier}
     */
    public boolean hasFixedIdentifier() {
        return fixedIdentifier;
    }
}
