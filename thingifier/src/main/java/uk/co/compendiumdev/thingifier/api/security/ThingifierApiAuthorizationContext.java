package uk.co.compendiumdev.thingifier.api.security;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Request details supplied to a Thingifier API authorizer.
 *
 * <p>The authorization context wraps the authentication context and adds the principal returned by
 * the authenticator. Authorizers can inspect route identifiers, path parameters, and the active
 * store to decide whether the authenticated principal can use this specific route.
 */
public final class ThingifierApiAuthorizationContext {

    private final ThingifierApiAuthenticationContext authenticationContext;
    private final Object principal;

    /**
     * Creates an authorization context from an authentication context and principal.
     *
     * @param authenticationContext request details used during authentication
     * @param principal application principal returned by the authenticator
     */
    public ThingifierApiAuthorizationContext(
            final ThingifierApiAuthenticationContext authenticationContext,
            final Object principal) {
        this.authenticationContext = authenticationContext;
        this.principal = principal;
    }

    /**
     * Returns the authentication context that produced this authorization context.
     *
     * @return authentication context
     */
    public ThingifierApiAuthenticationContext authenticationContext() {
        return authenticationContext;
    }

    /**
     * Returns the authenticated application principal.
     *
     * @return principal object, or null when the authenticator did not provide one
     */
    public Object principal() {
        return principal;
    }

    /**
     * Returns the security scheme name that selected the authenticator.
     *
     * @return security scheme name
     */
    public String schemeName() {
        return authenticationContext.schemeName();
    }

    /**
     * Returns the generated API verb being processed.
     *
     * @return routing verb
     */
    public RoutingVerb verb() {
        return authenticationContext.verb();
    }

    /**
     * Returns the generated API path for the request.
     *
     * @return request path
     */
    public String path() {
        return authenticationContext.path();
    }

    /**
     * Returns named path parameters extracted from the matched API spec pattern.
     *
     * @return immutable path-parameter map
     */
    public Map<String, String> pathParameters() {
        return authenticationContext.pathParameters();
    }

    /**
     * Returns one named path parameter.
     *
     * @param name parameter name
     * @return parameter value, or an empty string when it was not present
     */
    public String pathParameter(final String name) {
        return authenticationContext.pathParameter(name);
    }

    /**
     * Returns the mapped generated route.
     *
     * @return route abstraction
     */
    public ThingRoute route() {
        return authenticationContext.route();
    }

    /**
     * Returns request headers.
     *
     * @return request header block
     */
    public HttpHeadersBlock headers() {
        return authenticationContext.headers();
    }

    /**
     * Returns the active request context.
     *
     * @return request context with store/session information
     */
    public ThingifierRequestContext requestContext() {
        return authenticationContext.requestContext();
    }

    /**
     * Returns the active data scope selected for the authorized request.
     *
     * <p>If the authenticator returned a data-scope selection, this value reflects that trusted
     * post-auth decision rather than any earlier session/header-selected scope.
     *
     * @return active data-scope name
     */
    public String dataScopeName() {
        return authenticationContext.dataScopeName();
    }

    /**
     * Returns the active Thingifier store.
     *
     * @return request store
     */
    public ThingStore store() {
        return authenticationContext.store();
    }

    /**
     * Returns the parsed bearer token.
     *
     * @return bearer token without the Authorization scheme prefix
     */
    public String bearerToken() {
        return authenticationContext.bearerToken();
    }

    /**
     * Returns the parsed Basic username from the authentication step.
     *
     * @return Basic username, or an empty string when the enforced scheme is not Basic
     */
    public String basicUsername() {
        return authenticationContext.basicUsername();
    }

    /**
     * Returns the parsed Basic password from the authentication step.
     *
     * @return Basic password, or an empty string when the enforced scheme is not Basic
     */
    public String basicPassword() {
        return authenticationContext.basicPassword();
    }

    /**
     * Returns the target entity for entity or relationship routes.
     *
     * @return target entity, or null when no entity route matched
     */
    public EntityDefinition targetEntity() {
        return authenticationContext.targetEntity();
    }

    /**
     * Returns the target identifier for instance routes.
     *
     * @return target identifier, or null for collection routes
     */
    public String targetIdentifier() {
        return authenticationContext.targetIdentifier();
    }

    /**
     * Returns the parent entity for relationship routes.
     *
     * @return parent entity, or null for entity routes
     */
    public EntityDefinition parentEntity() {
        return authenticationContext.parentEntity();
    }

    /**
     * Returns the parent identifier for relationship routes.
     *
     * @return parent identifier, or null for entity routes
     */
    public String parentIdentifier() {
        return authenticationContext.parentIdentifier();
    }

    /**
     * Returns the relationship name for relationship routes.
     *
     * @return relationship name, or null for entity routes
     */
    public String relationshipName() {
        return authenticationContext.relationshipName();
    }

    /**
     * Returns the child identifier for relationship instance routes.
     *
     * @return child identifier, or null when the route has no child
     */
    public String childIdentifier() {
        return authenticationContext.childIdentifier();
    }
}
