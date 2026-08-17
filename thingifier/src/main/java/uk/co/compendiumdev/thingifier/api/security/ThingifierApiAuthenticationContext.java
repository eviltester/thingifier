package uk.co.compendiumdev.thingifier.api.security;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Request details supplied to a Thingifier API authenticator.
 *
 * <p>The context gives authentication code enough information to resolve a token against the active
 * store/session without exposing mutable request-processing internals.
 */
public final class ThingifierApiAuthenticationContext {

    private final String schemeName;
    private final RoutingVerb verb;
    private final String path;
    private final Map<String, String> pathParameters;
    private final ThingRoute route;
    private final HttpHeadersBlock headers;
    private final ThingifierRequestContext requestContext;
    private final String bearerToken;
    private final EntityDefinition targetEntity;
    private final String targetIdentifier;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    /**
     * Creates an immutable authentication context.
     *
     * @param details reusable route and request details
     * @param bearerToken parsed bearer token from the Authorization header
     */
    public ThingifierApiAuthenticationContext(
            final ThingifierApiRouteAuthDetails details, final String bearerToken) {
        this.schemeName = details.schemeName();
        this.verb = details.verb();
        this.path = details.path();
        this.pathParameters = details.pathParameters();
        this.route = details.route();
        this.headers = details.headers();
        this.requestContext = details.requestContext();
        this.bearerToken = bearerToken == null ? "" : bearerToken;
        this.targetEntity = details.targetEntity();
        this.targetIdentifier = details.targetIdentifier();
        this.parentEntity = details.parentEntity();
        this.parentIdentifier = details.parentIdentifier();
        this.relationshipName = details.relationshipName();
        this.childIdentifier = details.childIdentifier();
    }

    /**
     * Returns the security scheme name that selected the authenticator.
     *
     * @return security scheme name
     */
    public String schemeName() {
        return schemeName;
    }

    /**
     * Returns the generated API verb being processed.
     *
     * @return routing verb
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * Returns the generated API path for the request.
     *
     * @return request path
     */
    public String path() {
        return path;
    }

    /**
     * Returns named path parameters extracted from the matched API spec pattern.
     *
     * @return immutable path-parameter map
     */
    public Map<String, String> pathParameters() {
        return pathParameters;
    }

    /**
     * Returns one named path parameter.
     *
     * @param name parameter name
     * @return parameter value, or an empty string when it was not present
     */
    public String pathParameter(final String name) {
        return pathParameters.getOrDefault(name, "");
    }

    /**
     * Returns the mapped generated route.
     *
     * @return route abstraction
     */
    public ThingRoute route() {
        return route;
    }

    /**
     * Returns request headers.
     *
     * @return request header block
     */
    public HttpHeadersBlock headers() {
        return headers;
    }

    /**
     * Returns the active request context.
     *
     * @return request context with store/session information
     */
    public ThingifierRequestContext requestContext() {
        return requestContext;
    }

    /**
     * Returns the active Thingifier store.
     *
     * @return request store
     */
    public ThingStore store() {
        return requestContext.store();
    }

    /**
     * Returns the parsed bearer token.
     *
     * @return bearer token without the Authorization scheme prefix
     */
    public String bearerToken() {
        return bearerToken;
    }

    /**
     * Returns the target entity for entity or relationship routes.
     *
     * @return target entity, or null when no entity route matched
     */
    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    /**
     * Returns the target identifier for instance routes.
     *
     * @return target identifier, or null for collection routes
     */
    public String targetIdentifier() {
        return targetIdentifier;
    }

    /**
     * Returns the parent entity for relationship routes.
     *
     * @return parent entity, or null for entity routes
     */
    public EntityDefinition parentEntity() {
        return parentEntity;
    }

    /**
     * Returns the parent identifier for relationship routes.
     *
     * @return parent identifier, or null for entity routes
     */
    public String parentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Returns the relationship name for relationship routes.
     *
     * @return relationship name, or null for entity routes
     */
    public String relationshipName() {
        return relationshipName;
    }

    /**
     * Returns the child identifier for relationship instance routes.
     *
     * @return child identifier, or null when the route has no child
     */
    public String childIdentifier() {
        return childIdentifier;
    }
}
