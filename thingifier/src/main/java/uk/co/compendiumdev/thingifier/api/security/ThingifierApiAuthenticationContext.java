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
    private final String authCredential;
    private final String authCredentialSource;
    private final String bearerToken;
    private final String basicUsername;
    private final String basicPassword;
    private final String apiKey;
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
        this(details, bearerToken, "", "", "", bearerToken, "Authorization");
    }

    /**
     * Creates an immutable authentication context with parsed auth credentials.
     *
     * <p>Only the fields relevant to the enforced scheme are populated. Bearer routes receive a
     * bearer token, Basic routes receive username/password values, and API key routes use {@link
     * #apiKey(ThingifierApiRouteAuthDetails, String, String)}. Keeping one context type lets
     * applications use the same authenticator interface for every scheme.
     *
     * @param details reusable route and request details
     * @param bearerToken parsed bearer token from the Authorization header
     * @param basicUsername parsed Basic username from the Authorization header
     * @param basicPassword parsed Basic password from the Authorization header
     */
    public ThingifierApiAuthenticationContext(
            final ThingifierApiRouteAuthDetails details,
            final String bearerToken,
            final String basicUsername,
            final String basicPassword) {
        this(details, bearerToken, basicUsername, basicPassword, "", "", "Authorization");
    }

    /**
     * Creates an immutable authentication context for an API key header.
     *
     * <p>API key values are public API credentials read from a configured request header. The
     * source name is preserved so authenticators can distinguish schemes that intentionally share a
     * callback but use different headers.
     *
     * @param details reusable route and request details
     * @param apiKey parsed API key credential
     * @param headerName request header that supplied the credential
     * @return authentication context populated for API key auth
     */
    public static ThingifierApiAuthenticationContext apiKey(
            final ThingifierApiRouteAuthDetails details,
            final String apiKey,
            final String headerName) {
        return new ThingifierApiAuthenticationContext(
                details, "", "", "", apiKey, apiKey, headerName);
    }

    private ThingifierApiAuthenticationContext(
            final ThingifierApiRouteAuthDetails details,
            final String bearerToken,
            final String basicUsername,
            final String basicPassword,
            final String apiKey,
            final String authCredential,
            final String authCredentialSource) {
        this.schemeName = details.schemeName();
        this.verb = details.verb();
        this.path = details.path();
        this.pathParameters = details.pathParameters();
        this.route = details.route();
        this.headers = details.headers();
        this.requestContext = details.requestContext();
        this.authCredential = authCredential == null ? "" : authCredential;
        this.authCredentialSource = authCredentialSource == null ? "" : authCredentialSource;
        this.bearerToken = bearerToken == null ? "" : bearerToken;
        this.basicUsername = basicUsername == null ? "" : basicUsername;
        this.basicPassword = basicPassword == null ? "" : basicPassword;
        this.apiKey = apiKey == null ? "" : apiKey;
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
     * Returns the security scheme name that selected the authenticator.
     *
     * <p>This alias mirrors the public API wording used by generic authenticators. It returns the
     * same value as {@link #schemeName()}.
     *
     * @return security scheme name
     */
    public String authSchemeName() {
        return schemeName();
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
     * Returns the active data scope for this request.
     *
     * <p>During authentication this is the context selected before the authenticator returned. When
     * the authentication result selects a different data scope, authorizers and later lifecycle
     * phases see the updated value through the same request context.
     *
     * @return active data-scope name
     */
    public String dataScopeName() {
        return requestContext.dataScopeName();
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
     * Returns the parsed credential for token-style authentication schemes.
     *
     * <p>Bearer and API key routes populate this with the bearer token or API key. Basic auth
     * exposes its credential pair through {@link #basicUsername()} and {@link #basicPassword()}
     * instead of combining them into one string.
     *
     * @return parsed token-style credential, or an empty string for Basic auth
     */
    public String authCredential() {
        return authCredential;
    }

    /**
     * Returns where the parsed credential came from.
     *
     * <p>Bearer and Basic use {@code Authorization}. API key auth returns the configured public
     * header name, for example {@code X-AUTH-TOKEN}.
     *
     * @return credential source header name, or an empty string when unavailable
     */
    public String authCredentialSource() {
        return authCredentialSource;
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
     * Returns the parsed Basic username.
     *
     * @return Basic username, or an empty string when the enforced scheme is not Basic
     */
    public String basicUsername() {
        return basicUsername;
    }

    /**
     * Returns the parsed Basic password.
     *
     * @return Basic password, or an empty string when the enforced scheme is not Basic
     */
    public String basicPassword() {
        return basicPassword;
    }

    /**
     * Returns the parsed API key credential.
     *
     * @return API key value, or an empty string when the enforced scheme is not API key auth
     */
    public String apiKey() {
        return apiKey;
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
