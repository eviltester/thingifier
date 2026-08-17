package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;

/**
 * Holds generated route metadata used for API documentation and route registration.
 *
 * <p>The routing definition sits between the Thingifier model and adapters such as OpenAPI or the
 * HTTP server. API spec rules are applied here so generated documentation and runtime route
 * registration see the same visibility, payload, response view, and Allow-header metadata.
 */
public final class ApiRoutingDefinition {

    private List<RoutingDefinition> routings;
    private HashMap<String, EntityDefinition> objectSchemas;

    public ApiRoutingDefinition() {
        routings = new ArrayList<>();
        objectSchemas = new HashMap<String, EntityDefinition>();
    }

    /**
     * Returns all generated route definitions.
     *
     * @return mutable collection of route definitions maintained by the generator
     */
    public Collection<RoutingDefinition> definitions() {
        return routings;
    }

    /**
     * Adds a generated route without a special response header.
     *
     * @param documentation generated documentation text
     * @param verb route verb
     * @param url route path
     * @param routingStatus configured routing status behavior
     * @return the created route definition so callers can add payload metadata
     */
    public RoutingDefinition addRouting(
            final String documentation,
            final RoutingVerb verb,
            final String url,
            final RoutingStatus routingStatus) {
        RoutingDefinition defn =
                new RoutingDefinition(verb, url, routingStatus, null)
                        .addDocumentation(documentation);
        routings.add(defn);
        return defn;
    }

    /**
     * Adds a generated route with a response header template.
     *
     * @param documentation generated documentation text
     * @param verb route verb
     * @param url route path
     * @param routingStatus configured routing status behavior
     * @param header response header metadata attached to the route
     * @return the created route definition so callers can add payload metadata
     */
    public RoutingDefinition addRouting(
            final String documentation,
            final RoutingVerb verb,
            final String url,
            final RoutingStatus routingStatus,
            final ResponseHeader header) {
        RoutingDefinition defn =
                new RoutingDefinition(verb, url, routingStatus, header)
                        .addDocumentation(documentation);
        routings.add(defn);
        return defn;
    }

    /**
     * Registers the model entity under the schema names generated routes may reference.
     *
     * <p>View names and create payload names are mapped back to the owning entity so API spec
     * defaults can resolve from route payload metadata to entity view policy.
     *
     * @param entityDefn entity definition used by generated payload schemas
     */
    public void addObjectSchema(EntityDefinition entityDefn) {
        // TODO this should be an object schema rather than entityDefinition
        // because we don't want it to be editable
        // as single entity
        objectSchemas.put(entityDefn.getName(), entityDefn);

        // used for top level POST requests so there are no auto ids in the payload
        objectSchemas.put("create_" + entityDefn.getName(), entityDefn);

        // and as plural for array responses
        objectSchemas.put(entityDefn.getPlural(), entityDefn);

        for (EntityViewDefinition view : entityDefn.getViews()) {
            objectSchemas.put(view.getName(), entityDefn);
            objectSchemas.put("create_" + view.getName(), entityDefn);
        }
    }

    /**
     * Reports whether a generated payload schema name is known.
     *
     * @param aName schema name to look up
     * @return true when the schema name has been registered
     */
    public boolean hasObjectSchemaNamed(String aName) {
        return objectSchemas.containsKey(aName);
    }

    /**
     * Resolves a generated payload schema name back to the owning entity.
     *
     * <p>This powers entity-level default views because generated route metadata stores payload
     * schema names, while the view definitions live on the entity.
     *
     * @param name generated payload schema name
     * @return owning entity when the schema name is registered
     */
    public Optional<EntityDefinition> objectSchemaNamed(final String name) {
        return Optional.ofNullable(objectSchemas.get(name));
    }

    /**
     * Returns the entity definitions registered as object schemas.
     *
     * @return collection of registered schema entities
     */
    public Collection<EntityDefinition> getObjectSchemas() {
        return objectSchemas.values();
    }

    /**
     * Recomputes generated OPTIONS Allow headers from currently visible and callable routes.
     *
     * <p>Routes configured as method-not-allowed return a fixed 405 and are intentionally excluded
     * from Allow. Disabled and documentation-hidden routes are also omitted from the advertised
     * method list.
     */
    public void updateOptionsAllowHeaders() {
        final List<RoutingVerb> verbOrder =
                List.of(
                        RoutingVerb.OPTIONS,
                        RoutingVerb.GET,
                        RoutingVerb.HEAD,
                        RoutingVerb.POST,
                        RoutingVerb.QUERY,
                        RoutingVerb.PUT,
                        RoutingVerb.PATCH,
                        RoutingVerb.DELETE,
                        RoutingVerb.TRACE);
        final Map<String, List<RoutingVerb>> allowedByUrl = new HashMap<>();

        for (RoutingDefinition route : routings) {
            if (route.isHiddenFromDocumentation() || route.isDisabled()) {
                continue;
            }
            if (route.verb() == RoutingVerb.OPTIONS || route.status().isReturnedFromCall()) {
                allowedByUrl.computeIfAbsent(route.url(), key -> new ArrayList<>());
                if (!allowedByUrl.get(route.url()).contains(route.verb())) {
                    allowedByUrl.get(route.url()).add(route.verb());
                }
            }
        }

        for (RoutingDefinition route : routings) {
            if (route.verb() != RoutingVerb.OPTIONS || route.header().isEmpty()) {
                continue;
            }
            final List<RoutingVerb> allowed = allowedByUrl.getOrDefault(route.url(), List.of());
            final String allowHeader =
                    verbOrder.stream()
                            .filter(allowed::contains)
                            .map(Enum::name)
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("OPTIONS");
            route.replaceHeader(new ResponseHeader(route.header(), allowHeader));
        }
    }
}
