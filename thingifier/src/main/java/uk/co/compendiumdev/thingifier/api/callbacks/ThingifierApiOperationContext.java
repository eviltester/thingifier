package uk.co.compendiumdev.thingifier.api.callbacks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Immutable route and request context supplied to route operation callbacks.
 *
 * <p>The context reflects trusted routing, authentication, and data-scope decisions that have
 * already happened before the operation callback runs. Applications can use it to update app-owned
 * state without reparsing public paths or re-discovering the active store.
 */
public final class ThingifierApiOperationContext {

    private final RoutingVerb verb;
    private final String publicPath;
    private final String mountedPath;
    private final String internalPath;
    private final String mountName;
    private final String mountPrefix;
    private final ThingRoute route;
    private final ThingifierApiRouteRule routeRule;
    private final String targetEntityName;
    private final String targetIdentifier;
    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;
    private final String dataScopeName;
    private final ThingStore store;
    private final Map<String, Object> authenticatedPrincipals;
    private final HttpHeadersBlock requestHeaders;
    private final QueryFilterParams queryParams;
    private final ApiBodyFields parsedRequestBody;
    private final String rawRequestBody;
    private final ThingifierApiConfig apiConfig;

    /**
     * Creates a callback context.
     *
     * @param verb route verb being processed
     * @param publicPath public request path
     * @param route resolved generated route
     * @param routeRule matched route rule that owns the callback
     * @param targetEntityName target entity name, or null
     * @param targetIdentifier target identifier, or null
     * @param parentEntityName relationship parent entity name, or null
     * @param parentIdentifier relationship parent identifier, or null
     * @param relationshipName relationship route name, or null
     * @param childIdentifier relationship child identifier, or null
     * @param dataScopeName active data-scope name
     * @param store active store
     * @param authenticatedPrincipals authenticated principals by scheme name
     * @param requestHeaders request headers
     * @param queryParams parsed query parameters
     * @param parsedRequestBody parsed body fields
     * @param rawRequestBody raw request body text
     * @param apiConfig active API configuration
     */
    public ThingifierApiOperationContext(
            final RoutingVerb verb,
            final String publicPath,
            final ThingRoute route,
            final ThingifierApiRouteRule routeRule,
            final String targetEntityName,
            final String targetIdentifier,
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier,
            final String dataScopeName,
            final ThingStore store,
            final Map<String, Object> authenticatedPrincipals,
            final HttpHeadersBlock requestHeaders,
            final QueryFilterParams queryParams,
            final ApiBodyFields parsedRequestBody,
            final String rawRequestBody,
            final ThingifierApiConfig apiConfig) {
        this(
                verb,
                publicPath,
                publicPath,
                publicPath,
                null,
                "",
                route,
                routeRule,
                targetEntityName,
                targetIdentifier,
                parentEntityName,
                parentIdentifier,
                relationshipName,
                childIdentifier,
                dataScopeName,
                store,
                authenticatedPrincipals,
                requestHeaders,
                queryParams,
                parsedRequestBody,
                rawRequestBody,
                apiConfig);
    }

    /**
     * Creates a callback context with explicit mounted route details.
     *
     * <p>Mounted requests expose both the public path requested by the caller and the canonical
     * internal path processed by Thingifier. Keeping both values here lets application callbacks
     * make route-aware decisions without guessing which prefix was stripped.
     *
     * @param verb route verb being processed
     * @param publicPath public request path
     * @param mountedPath active mounted path
     * @param internalPath canonical Thingifier route path
     * @param mountName active mount name, or null
     * @param mountPrefix active mount prefix, or empty
     * @param route resolved generated route
     * @param routeRule matched route rule that owns the callback
     * @param targetEntityName target entity name, or null
     * @param targetIdentifier target identifier, or null
     * @param parentEntityName relationship parent entity name, or null
     * @param parentIdentifier relationship parent identifier, or null
     * @param relationshipName relationship route name, or null
     * @param childIdentifier relationship child identifier, or null
     * @param dataScopeName active data-scope name
     * @param store active store
     * @param authenticatedPrincipals authenticated principals by scheme name
     * @param requestHeaders request headers
     * @param queryParams parsed query parameters
     * @param parsedRequestBody parsed body fields
     * @param rawRequestBody raw request body text
     * @param apiConfig active API configuration
     */
    public ThingifierApiOperationContext(
            final RoutingVerb verb,
            final String publicPath,
            final String mountedPath,
            final String internalPath,
            final String mountName,
            final String mountPrefix,
            final ThingRoute route,
            final ThingifierApiRouteRule routeRule,
            final String targetEntityName,
            final String targetIdentifier,
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final String childIdentifier,
            final String dataScopeName,
            final ThingStore store,
            final Map<String, Object> authenticatedPrincipals,
            final HttpHeadersBlock requestHeaders,
            final QueryFilterParams queryParams,
            final ApiBodyFields parsedRequestBody,
            final String rawRequestBody,
            final ThingifierApiConfig apiConfig) {
        this.verb = verb;
        this.publicPath = normalizedPublicPath(publicPath);
        this.mountedPath = normalizedPublicPath(mountedPath);
        this.internalPath = normalizedPublicPath(internalPath);
        this.mountName = mountName;
        this.mountPrefix = normalizedMountPrefix(mountPrefix);
        this.route = route;
        this.routeRule = routeRule;
        this.targetEntityName = targetEntityName;
        this.targetIdentifier = targetIdentifier;
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childIdentifier = childIdentifier;
        this.dataScopeName = dataScopeName;
        this.store = store;
        this.authenticatedPrincipals =
                Map.copyOf(
                        authenticatedPrincipals == null
                                ? Map.of()
                                : new HashMap<>(authenticatedPrincipals));
        this.requestHeaders = copyHeaders(requestHeaders);
        this.queryParams = copyQueryParams(queryParams);
        this.parsedRequestBody =
                parsedRequestBody == null ? ApiBodyFields.empty() : parsedRequestBody;
        this.rawRequestBody = rawRequestBody == null ? "" : rawRequestBody;
        this.apiConfig = apiConfig;
    }

    /**
     * @return route verb being processed
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * @return public path requested by the caller
     */
    public String publicPath() {
        return publicPath;
    }

    /**
     * Returns the active mounted path requested by the caller.
     *
     * @return mounted path with a leading slash
     */
    public String mountedPath() {
        return mountedPath;
    }

    /**
     * Returns the canonical Thingifier path processed by generated handlers.
     *
     * @return internal route path with a leading slash
     */
    public String internalPath() {
        return internalPath;
    }

    /**
     * Returns the active public mount name.
     *
     * @return mount name when a named mount matched, otherwise empty
     */
    public Optional<String> mountName() {
        return Optional.ofNullable(mountName);
    }

    /**
     * Returns the active public mount prefix.
     *
     * @return mount prefix with a leading slash, or empty when no prefix applies
     */
    public String mountPrefix() {
        return mountPrefix;
    }

    /**
     * @return resolved generated route target
     */
    public ThingRoute route() {
        return route;
    }

    /**
     * @return matched route rule that registered the callback
     */
    public ThingifierApiRouteRule routeRule() {
        return routeRule;
    }

    /**
     * @return route pattern declared on the matched route rule
     */
    public String matchedRoutePattern() {
        return routeRule == null ? publicPath : routeRule.pathPattern();
    }

    /**
     * @return targeted entity name when the route is entity-backed
     */
    public Optional<String> targetEntityName() {
        return Optional.ofNullable(targetEntityName);
    }

    /**
     * @return targeted identifier for instance and fixed routes
     */
    public Optional<String> targetIdentifier() {
        return Optional.ofNullable(targetIdentifier);
    }

    /**
     * @return relationship parent entity name when applicable
     */
    public Optional<String> parentEntityName() {
        return Optional.ofNullable(parentEntityName);
    }

    /**
     * @return relationship parent identifier when applicable
     */
    public Optional<String> parentIdentifier() {
        return Optional.ofNullable(parentIdentifier);
    }

    /**
     * @return relationship route name when applicable
     */
    public Optional<String> relationshipName() {
        return Optional.ofNullable(relationshipName);
    }

    /**
     * @return relationship child identifier when applicable
     */
    public Optional<String> childIdentifier() {
        return Optional.ofNullable(childIdentifier);
    }

    /**
     * @return active data-scope name after authentication had a chance to select it
     */
    public String dataScopeName() {
        return dataScopeName;
    }

    /**
     * @return active Thingifier store for the operation
     */
    public ThingStore store() {
        return store;
    }

    /**
     * Returns the principal for the common single-scheme case.
     *
     * @return first authenticated principal, or null when none was stored
     */
    public Object authenticatedPrincipal() {
        if (authenticatedPrincipals.isEmpty()) {
            return null;
        }
        return authenticatedPrincipals.values().iterator().next();
    }

    /**
     * Returns the principal for a named security scheme.
     *
     * @param schemeName security scheme name
     * @return authenticated principal, or null
     */
    public Object authenticatedPrincipal(final String schemeName) {
        return authenticatedPrincipals.get(schemeName);
    }

    /**
     * @return authenticated principals keyed by scheme name
     */
    public Map<String, Object> authenticatedPrincipals() {
        return authenticatedPrincipals;
    }

    /**
     * @return copy of request headers
     */
    public HttpHeadersBlock requestHeaders() {
        return copyHeaders(requestHeaders);
    }

    /**
     * @return copy of parsed query parameters
     */
    public QueryFilterParams queryParams() {
        return copyQueryParams(queryParams);
    }

    /**
     * @return parsed request body fields, or empty fields when no body was parsed
     */
    public ApiBodyFields parsedRequestBody() {
        return parsedRequestBody;
    }

    /**
     * @return raw request body text, or an empty string when no body was supplied
     */
    public String rawRequestBody() {
        return rawRequestBody;
    }

    /**
     * @return active API configuration
     */
    public ThingifierApiConfig apiConfig() {
        return apiConfig;
    }

    private HttpHeadersBlock copyHeaders(final HttpHeadersBlock source) {
        HttpHeadersBlock copy = new HttpHeadersBlock();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private QueryFilterParams copyQueryParams(final QueryFilterParams source) {
        QueryFilterParams copy = new QueryFilterParams();
        if (source != null) {
            copy.addAll(source);
        }
        return copy;
    }

    private String normalizedPublicPath(final String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String normalizedMountPrefix(final String prefix) {
        if (prefix == null || prefix.isEmpty() || "/".equals(prefix)) {
            return prefix == null ? "" : prefix;
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
