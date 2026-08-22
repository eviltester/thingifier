package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.SchemaCatalog;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;

/**
 * Resolves public API paths into the internal route model used by Thingifier handlers.
 *
 * <p>Most requests map directly from the generated URL shape. Fixed-instance routes are different:
 * their public URL has no identifier, but the API spec declares the entity and identifier that
 * should be used internally. Centralising that decision keeps auth, lifecycle hooks, write policy,
 * and command/query mapping aligned.
 */
public final class ThingRouteResolver {

    private final SchemaCatalog schema;
    private final ThingifierApiSpec apiSpec;
    private final String apiPathPrefix;
    private final ThingRouteMapper generatedRouteMapper;

    /**
     * Creates a route resolver for one API runtime.
     *
     * @param schema schema catalogue used to resolve entity names
     * @param apiSpec route policy and fixed-route declarations
     * @param apiPathPrefix configured API prefix used when matching route rules
     */
    public ThingRouteResolver(
            final SchemaCatalog schema,
            final ThingifierApiSpec apiSpec,
            final String apiPathPrefix) {
        this.schema = schema;
        this.apiSpec = apiSpec;
        this.apiPathPrefix = apiPathPrefix == null ? "" : apiPathPrefix;
        this.generatedRouteMapper = new ThingRouteMapper(schema);
    }

    /**
     * Maps a public path to the route shape handlers should process.
     *
     * @param verb routing verb for the request
     * @param path public request path
     * @return fixed internal instance route when configured, otherwise the generated route mapping
     */
    public ThingRoute map(final RoutingVerb verb, final String path) {
        Optional<ThingifierApiRouteRule> fixedRoute =
                apiSpec.fixedRouteRuleFor(verb, path, apiPathPrefix);
        if (fixedRoute.isPresent()) {
            return fixedInstanceRoute(path, fixedRoute.get());
        }
        return generatedRouteMapper.map(path);
    }

    private ThingRoute fixedInstanceRoute(
            final String path, final ThingifierApiRouteRule fixedRoute) {
        EntityTypeRef entity = schema.entityWithSingularOrPluralName(fixedRoute.fixedEntityName());
        if (entity == null) {
            return new UnmatchedRoute(path, ThingRouteMapper.parts(path));
        }
        return new InstanceRoute(path, entity, fixedRoute.fixedIdentifier(), true);
    }
}
