package uk.co.compendiumdev.thingifier.api.validation;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Immutable request-aware context supplied to route-level API operation validators.
 *
 * <p>This context exists because some API rules are about the public operation rather than the
 * entity model: they may depend on authentication, selected data scope, the public fixed route, or
 * route-specific request body semantics. The authenticator and route matcher have already made
 * trusted decisions before this context is built.
 */
public final class ApiOperationValidationContext {

    private final RoutingVerb verb;
    private final String publicPath;
    private final ThingRoute route;
    private final String targetEntityName;
    private final String targetIdentifier;
    private final String operationType;
    private final ThingifierRequestContext requestContext;
    private final HttpHeadersBlock headers;
    private final QueryFilterParams queryParameters;
    private final ApiBodyFields requestBody;
    private final String rawBody;
    private final String requestEntityView;
    private final String responseEntityView;

    /**
     * Creates an operation validation context.
     *
     * @param verb route verb being processed
     * @param publicPath public API path requested by the caller
     * @param route resolved Thingifier route target
     * @param targetEntityName target entity name, or null when the route is not entity-backed
     * @param targetIdentifier target instance identifier, or null for collection routes
     * @param operationType read/write operation label such as CREATE, UPDATE, DELETE, or QUERY
     * @param requestContext active request context after authentication and data-scope selection
     * @param headers request headers
     * @param queryParameters parsed query parameters
     * @param requestBody parsed request body fields
     * @param rawBody raw request body text
     * @param requestEntityView request entity view selected for this route, or null
     * @param responseEntityView response entity view selected for the expected success status, or
     *     null
     */
    public ApiOperationValidationContext(
            final RoutingVerb verb,
            final String publicPath,
            final ThingRoute route,
            final String targetEntityName,
            final String targetIdentifier,
            final String operationType,
            final ThingifierRequestContext requestContext,
            final HttpHeadersBlock headers,
            final QueryFilterParams queryParameters,
            final ApiBodyFields requestBody,
            final String rawBody,
            final String requestEntityView,
            final String responseEntityView) {
        this.verb = verb;
        this.publicPath = publicPath == null ? "" : publicPath;
        this.route = route;
        this.targetEntityName = targetEntityName;
        this.targetIdentifier = targetIdentifier;
        this.operationType = operationType == null ? "" : operationType;
        this.requestContext = requestContext;
        this.headers = copyHeaders(headers);
        this.queryParameters = copyQueryParameters(queryParameters);
        this.requestBody = requestBody == null ? ApiBodyFields.empty() : requestBody;
        this.rawBody = rawBody == null ? "" : rawBody;
        this.requestEntityView = requestEntityView;
        this.responseEntityView = responseEntityView;
    }

    /**
     * Returns the route verb being processed.
     *
     * @return routing verb
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * Returns the public API path requested by the caller.
     *
     * @return public path
     */
    public String publicPath() {
        return publicPath;
    }

    /**
     * Returns the resolved Thingifier route target.
     *
     * @return route object used by generated handlers
     */
    public ThingRoute route() {
        return route;
    }

    /**
     * Returns the entity targeted by this operation.
     *
     * @return entity name, or null when not entity-backed
     */
    public String targetEntityName() {
        return targetEntityName;
    }

    /**
     * Returns the target instance identifier.
     *
     * @return identifier, or null for collection operations
     */
    public String targetIdentifier() {
        return targetIdentifier;
    }

    /**
     * Returns the resolved operation type.
     *
     * <p>Generated writes expose concrete operation names such as CREATE, UPDATE,
     * CREATE_AND_CONNECT, UPDATE_CONNECTED, DISCONNECT, and DELETE so validators do not have to
     * infer intent from route shape.
     *
     * @return operation type label
     */
    public String operationType() {
        return operationType;
    }

    /**
     * Returns the active data-scope name after authentication has had a chance to select it.
     *
     * @return data-scope name
     */
    public String dataScopeName() {
        return requestContext == null ? null : requestContext.dataScopeName();
    }

    /**
     * Returns the active Thingifier store after data-scope selection.
     *
     * @return active store, or null when no request context is available
     */
    public ThingStore store() {
        return requestContext == null ? null : requestContext.store();
    }

    /**
     * Returns the authenticated principal for the usual single-scheme route case.
     *
     * @return authenticated principal, or null when none has been stored
     */
    public Object authenticatedPrincipal() {
        Map<String, Object> principals = authenticatedPrincipals();
        if (principals.isEmpty()) {
            return null;
        }
        return principals.values().iterator().next();
    }

    /**
     * Returns the authenticated principal for a named security scheme.
     *
     * @param schemeName security scheme name
     * @return authenticated principal, or null when that scheme did not authenticate
     */
    public Object authenticatedPrincipal(final String schemeName) {
        return requestContext == null ? null : requestContext.authenticatedPrincipal(schemeName);
    }

    /**
     * Returns all authenticated principals by security scheme.
     *
     * @return immutable copy of authenticated principal entries
     */
    public Map<String, Object> authenticatedPrincipals() {
        return requestContext == null ? Map.of() : requestContext.authenticatedPrincipals();
    }

    /**
     * Returns a copy of the request headers.
     *
     * @return request headers
     */
    public HttpHeadersBlock headers() {
        return copyHeaders(headers);
    }

    /**
     * Returns a copy of the parsed query parameters.
     *
     * @return query parameters
     */
    public QueryFilterParams queryParameters() {
        return copyQueryParameters(queryParameters);
    }

    /**
     * Returns parsed body fields.
     *
     * @return request body fields, never null
     */
    public ApiBodyFields requestBody() {
        return requestBody;
    }

    /**
     * Returns the raw request body text.
     *
     * @return raw body, never null
     */
    public String rawBody() {
        return rawBody;
    }

    /**
     * Returns the route/entity request view selected before operation validation.
     *
     * @return request entity view name, or null
     */
    public String requestEntityView() {
        return requestEntityView;
    }

    /**
     * Returns the route/entity response view selected for the expected success response.
     *
     * @return response entity view name, or null
     */
    public String responseEntityView() {
        return responseEntityView;
    }

    private HttpHeadersBlock copyHeaders(final HttpHeadersBlock source) {
        HttpHeadersBlock copy = new HttpHeadersBlock();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private QueryFilterParams copyQueryParameters(final QueryFilterParams source) {
        QueryFilterParams copy = new QueryFilterParams();
        copy.addAll(source);
        return copy;
    }
}
