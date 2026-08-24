package uk.co.compendiumdev.thingifier.api;

import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.FixedRouteResourcePreparer;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RouteApiResponsePolicyApplier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RouteAuthPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RouteOperationCallbackApplier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ScopedSessionPolicyApplier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.EntityResponseViewResolver;
import uk.co.compendiumdev.thingifier.api.restapihandlers.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Direct Thingifier API facade for REST-style operations.
 *
 * <p>This class is used both by in-process callers and by the HTTP API adapter. It applies the same
 * response policy as HTTP routing so direct calls and server calls agree on relationship rendering,
 * route response views, and entity-level default response views.
 */
public class ThingifierRestAPIHandler {
    private final ThingifierApiRuntime runtime;
    private final RestApiDeleteHandler delete;
    private final RestApiPostHandler post;
    private final RestApiPutHandler put;
    private final RestApiPatchHandler patch;
    private final RestApiGetHandler get;
    private final RestApiQueryHandler query;
    private final RouteAuthPolicy authPolicy;
    private final ScopedSessionPolicyApplier scopedSessionPolicy;

    /**
     * Creates a direct API facade for a Thingifier model.
     *
     * @param aThingifier Thingifier model and runtime configuration
     */
    public ThingifierRestAPIHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a direct API facade using an explicit runtime.
     *
     * @param runtime runtime services used by the generated API handlers
     */
    public ThingifierRestAPIHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a direct API facade using explicit runtime services and lifecycle hooks.
     *
     * <p>The lifecycle registry is supplied by HTTP routing when a request is being processed
     * through the full lifecycle. Direct callers that do not need hooks can use the simpler
     * constructors.
     *
     * @param runtime runtime services used by the generated API handlers
     * @param lifecycleHooks lifecycle hook registry, or null for an empty registry
     */
    public ThingifierRestAPIHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        ThingifierApiLifecycleHookRegistry hooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
        this.get = new RestApiGetHandler(runtime, hooks);
        this.delete = new RestApiDeleteHandler(runtime, hooks);
        this.post = new RestApiPostHandler(runtime, hooks);
        this.put = new RestApiPutHandler(runtime, hooks);
        this.patch = new RestApiPatchHandler(runtime, hooks);
        this.query = new RestApiQueryHandler(runtime, hooks);
        this.authPolicy = new RouteAuthPolicy(runtime);
        this.scopedSessionPolicy = new ScopedSessionPolicyApplier(runtime);
    }

    // TODO: we should be able to accept xml with correct content type
    // TODO: we should be able to accept html forms with correct content type
    // todo allow an accept text/html to create different output - (probably handled by routings
    // rather than api)
    // todo : generate examples when outputing the api documentation

    // TODO: - listed here
    // https://www.lisihocke.com/2018/07/testing-tour-stop-16-pair-exploring-an-api-with-thomas.html
    // TODO: ensure that relationshps enforce the type of thing e.g. if I pass in a GUID of the
    // wrong type then it should not cross ref
    // TODO: possibly consider an X- header which has the number of items in the collection

    /**
     * Handles a GET request using discrete direct-call arguments.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse get(
            final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.GET,
                url,
                context,
                null,
                queryParams,
                () -> get.handle(url, queryParams, context));
    }

    /**
     * Handles a GET request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse get(final ApiRequestEnvelope request) {
        return get(request, null);
    }

    /**
     * Handles a GET request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse get(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        final RoutingVerb verb = lifecycle == null ? RoutingVerb.GET : lifecycle.routingVerb();
        return withAuthorizedResponsePolicy(
                verb,
                request.path(),
                context,
                lifecycle,
                request,
                () -> get.handle(request.path(), request.queryParams(), context, lifecycle));
    }

    /**
     * Handles a HEAD request using discrete direct-call arguments.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param headers request headers used to resolve request context
     * @return API response with any body cleared after GET semantics are evaluated
     */
    public ApiResponse head(
            final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.HEAD,
                url,
                context,
                null,
                queryParams,
                () -> {
                    final ApiResponse response =
                            get.handle(RoutingVerb.HEAD, url, queryParams, context);
                    response.clearBody();
                    return response;
                });
    }

    /**
     * Handles a HEAD request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with any body cleared after GET semantics are evaluated
     */
    public ApiResponse head(final ApiRequestEnvelope request) {
        return head(request, null);
    }

    /**
     * Handles a HEAD request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with any body cleared after GET semantics are evaluated
     */
    public ApiResponse head(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.HEAD,
                request.path(),
                context,
                lifecycle,
                request,
                () -> {
                    final ApiResponse response =
                            get.handle(request.path(), request.queryParams(), context, lifecycle);
                    response.clearBody();
                    return response;
                });
    }

    /**
     * Handles a QUERY request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse query(final ApiRequestEnvelope request) {
        return query(request, null);
    }

    /**
     * Handles a QUERY request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse query(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.QUERY,
                request.path(),
                context,
                lifecycle,
                request,
                () ->
                        query.handle(
                                request.path(),
                                request.queryParams(),
                                request.queryBodyFormat(),
                                request.body(),
                                context,
                                lifecycle));
    }

    /**
     * Handles a DELETE request using discrete direct-call arguments.
     *
     * @param url generated API path
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse delete(final String url, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.DELETE, url, context, null, () -> delete.handle(url, context));
    }

    /**
     * Handles a DELETE request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse delete(final ApiRequestEnvelope request) {
        return delete(request, null);
    }

    /**
     * Handles a DELETE request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse delete(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.DELETE,
                request.path(),
                context,
                lifecycle,
                request,
                () -> delete.handle(request.path(), request.queryParams(), context, lifecycle));
    }

    /**
     * Handles a POST request using a body parser.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse post(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.POST,
                url,
                context,
                null,
                () ->
                        post.handle(
                                url,
                                args.bodyFields(),
                                args.rawBody(),
                                new QueryFilterParams(),
                                context,
                                null));
    }

    /**
     * Handles a POST request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse post(final ApiRequestEnvelope request) {
        return post(request, null);
    }

    /**
     * Handles a POST request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse post(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.POST,
                request.path(),
                context,
                lifecycle,
                request,
                () ->
                        post.handle(
                                request.path(),
                                request.bodyFields(),
                                request.body(),
                                request.queryParams(),
                                context,
                                lifecycle));
    }

    /**
     * Handles a POST request using already parsed body fields and request context.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context including store selection
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse post(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return withAuthorizedResponsePolicy(
                RoutingVerb.POST, url, context, null, () -> post.handle(url, bodyFields, context));
    }

    /**
     * Handles a PUT request using a body parser.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse put(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.PUT,
                url,
                context,
                null,
                () ->
                        put.handle(
                                url,
                                args.bodyFields(),
                                args.rawBody(),
                                new QueryFilterParams(),
                                context,
                                null));
    }

    /**
     * Handles a PUT request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse put(final ApiRequestEnvelope request) {
        return put(request, null);
    }

    /**
     * Handles a PUT request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse put(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.PUT,
                request.path(),
                context,
                lifecycle,
                request,
                () ->
                        put.handle(
                                request.path(),
                                request.bodyFields(),
                                request.body(),
                                request.queryParams(),
                                context,
                                lifecycle));
    }

    /**
     * Handles a PUT request using already parsed body fields and request context.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context including store selection
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse put(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return withAuthorizedResponsePolicy(
                RoutingVerb.PUT, url, context, null, () -> put.handle(url, bodyFields, context));
    }

    /**
     * Handles a PATCH request using a body parser.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse patch(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.PATCH,
                url,
                context,
                null,
                () -> patch.handle(url, args.rawBody(), headers, context));
    }

    /**
     * Handles a PATCH request using raw body text.
     *
     * @param url generated API path
     * @param body raw patch body
     * @param headers request headers used to resolve request context
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse patch(final String url, final String body, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withAuthorizedResponsePolicy(
                RoutingVerb.PATCH,
                url,
                context,
                null,
                () -> patch.handle(url, body, headers, context));
    }

    /**
     * Handles a PATCH request from a parsed request envelope.
     *
     * @param request parsed request envelope
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse patch(final ApiRequestEnvelope request) {
        return patch(request, null);
    }

    /**
     * Handles a PATCH request from a parsed request envelope and lifecycle context.
     *
     * @param request parsed request envelope
     * @param lifecycle lifecycle context for hook-enabled HTTP processing, or null
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse patch(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers(), lifecycle);
        return withAuthorizedResponsePolicy(
                RoutingVerb.PATCH,
                request.path(),
                context,
                lifecycle,
                request,
                () ->
                        patch.handle(
                                request.path(),
                                request.body(),
                                request.headers(),
                                request.queryParams(),
                                context,
                                lifecycle));
    }

    /**
     * Handles a PATCH request using raw body text and an explicit request context.
     *
     * @param url generated API path
     * @param body raw patch body
     * @param headers request headers used by patch format handling
     * @param context request context including store selection
     * @return API response with repository and response-view policy applied
     */
    public ApiResponse patch(
            final String url,
            final String body,
            final HttpHeadersBlock headers,
            final ThingifierRequestContext context) {
        return withAuthorizedResponsePolicy(
                RoutingVerb.PATCH,
                url,
                context,
                null,
                () -> patch.handle(url, body, headers, context));
    }

    private ThingifierRequestContext contextFrom(final HttpHeadersBlock headers) {
        return runtime.contextFrom(headers);
    }

    /**
     * Returns the lifecycle context when available, otherwise creates a direct-call context.
     *
     * <p>Lifecycle routing may have already stored an authenticated principal on the request
     * context, so lifecycle-backed handler calls must reuse it.
     *
     * @param headers request headers used for direct context creation
     * @param lifecycle lifecycle context, or null for direct calls
     * @return active request context
     */
    private ThingifierRequestContext contextFrom(
            final HttpHeadersBlock headers, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null) {
            return lifecycle.requestContext();
        }
        return contextFrom(headers);
    }

    /**
     * Applies route auth policy before executing a generated API handler.
     *
     * <p>HTTP lifecycle routing runs auth before body parsing, so lifecycle-backed calls skip this
     * direct gate to avoid authenticating twice. Direct calls pass null lifecycle and are checked
     * here before validation or mutation.
     *
     * @param verb routing verb used for auth and response-view lookup
     * @param url generated API path
     * @param context request context containing the active store
     * @param lifecycle lifecycle context when called through HTTP processing, otherwise null
     * @param action handler action to run when auth allows the request
     * @return response after auth and normal response policy have been applied
     */
    private ApiResponse withAuthorizedResponsePolicy(
            final RoutingVerb verb,
            final String url,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle,
            final Supplier<ApiResponse> action) {
        return withAuthorizedResponsePolicy(
                verb, url, context, lifecycle, null, new QueryFilterParams(), action);
    }

    /**
     * Applies scoped sessions and route auth for older direct-call helpers with query parameters.
     *
     * @param verb routing verb used for route-rule lookup
     * @param url generated API path
     * @param context request context containing the active store
     * @param lifecycle lifecycle context when called through HTTP processing, otherwise null
     * @param queryParams query parameters available to scoped-session credential resolution
     * @param action handler action to run when auth allows the request
     * @return response after auth and normal response policy have been applied
     */
    private ApiResponse withAuthorizedResponsePolicy(
            final RoutingVerb verb,
            final String url,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle,
            final QueryFilterParams queryParams,
            final Supplier<ApiResponse> action) {
        return withAuthorizedResponsePolicy(
                verb, url, context, lifecycle, null, queryParams, action);
    }

    /**
     * Applies auth, fixed-resource preparation, response policy, and route operation callbacks.
     *
     * <p>Callbacks run after response policies so they see the route-shaped API result, and before
     * legacy HTTP response hooks so application code still has one final compatibility hook phase.
     *
     * @param verb routing verb used for route-rule lookup
     * @param url generated API path
     * @param context request context containing the active store
     * @param lifecycle lifecycle context when called through HTTP processing, otherwise null
     * @param request parsed request envelope, or null for older direct-call helpers
     * @param action handler action to run when auth allows the request
     * @return response after auth, response policy, and route callbacks have been applied
     */
    private ApiResponse withAuthorizedResponsePolicy(
            final RoutingVerb verb,
            final String url,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request,
            final Supplier<ApiResponse> action) {
        return withAuthorizedResponsePolicy(
                verb,
                url,
                context,
                lifecycle,
                request,
                request == null ? new QueryFilterParams() : request.queryParams(),
                action);
    }

    /**
     * Applies scoped sessions, auth, fixed-resource preparation, response policy, and callbacks.
     *
     * <p>Scoped-session resolution runs before explicit route auth so route auth can override the
     * selected data scope when both are configured. Lifecycle-backed HTTP calls skip these gates
     * because {@link ThingifierHttpApi} has already run them before request validation.
     *
     * @param verb routing verb used for route-rule lookup
     * @param url generated API path
     * @param context request context containing the active store
     * @param lifecycle lifecycle context when called through HTTP processing, otherwise null
     * @param request parsed request envelope, or null for older direct-call helpers
     * @param queryParams query parameters available to scoped-session credential resolution
     * @param action handler action to run when auth allows the request
     * @return response after auth, response policy, and route callbacks have been applied
     */
    private ApiResponse withAuthorizedResponsePolicy(
            final RoutingVerb verb,
            final String url,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request,
            final QueryFilterParams queryParams,
            final Supplier<ApiResponse> action) {
        final ThingRoute route =
                lifecycle == null ? runtime.routeFor(verb, url) : lifecycle.route();
        final ApiResponse scopedSessionResponse =
                lifecycle == null
                        ? scopedSessionPolicy.rejectIfNotResolved(
                                verb, url, context, route, queryParams)
                        : null;
        if (scopedSessionResponse != null) {
            return withResponsePolicy(
                    verb, url, scopedSessionResponse, context, lifecycle, request);
        }

        final ApiResponse authResponse =
                lifecycle == null
                        ? authPolicy.rejectIfNotAuthorized(verb, url, context, route)
                        : null;
        if (authResponse != null) {
            return withResponsePolicy(verb, url, authResponse, context, lifecycle, request);
        }
        final ApiResponse fixedResourceResponse =
                new FixedRouteResourcePreparer(runtime).prepare(verb, url, route, context);
        if (fixedResourceResponse != null) {
            return withResponsePolicy(
                    verb, url, fixedResourceResponse, context, lifecycle, request);
        }
        return withResponsePolicy(verb, url, action.get(), context, lifecycle, request);
    }

    /**
     * Attaches the active relationship repository to responses that need serialization support.
     *
     * @param response response returned by a verb handler
     * @param context request context containing the active store
     * @return the same response with relationship repository metadata, or null
     */
    private ApiResponse withRepository(
            final ApiResponse response, final ThingifierRequestContext context) {
        if (response == null) {
            return null;
        }
        return response.usingRelationships(context.store().relationships());
    }

    /**
     * Applies shared direct-API response policy after a verb handler runs.
     *
     * <p>This keeps direct API calls aligned with HTTP calls for relationship rendering and
     * route/entity response views.
     *
     * @param verb routing verb used for route rule lookup
     * @param url generated API path
     * @param response response returned by the verb handler
     * @param context request context containing the active store
     * @return response after repository and response-view policy have been applied
     */
    private ApiResponse withResponsePolicy(
            final RoutingVerb verb,
            final String url,
            final ApiResponse response,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request) {
        final ApiResponse responseWithRepository = withRepository(response, context);
        final ApiResponse policyResponse =
                new RouteApiResponsePolicyApplier(runtime)
                        .apply(
                                verb,
                                url,
                                responseWithRepository,
                                context.headers(),
                                apiResponse -> applyResponseEntityView(verb, url, apiResponse));
        return new RouteOperationCallbackApplier(runtime)
                .apply(verb, url, policyResponse, context, lifecycle, request);
    }

    /**
     * Applies the response entity view chosen by route or entity-level API spec rules.
     *
     * <p>Route status-specific views win over entity defaults. Existing response overrides and
     * error bodies are left alone because they already control their rendered shape.
     *
     * @param verb routing verb used for route rule lookup
     * @param url generated API path
     * @param response response to update in place
     */
    private void applyResponseEntityView(
            final RoutingVerb verb, final String url, final ApiResponse response) {
        if (response == null
                || response.isErrorResponse()
                || response.hasABodyOverride()
                || response.getTypeOfThingReturned() == null) {
            return;
        }

        final EntityDefinition entity = response.getTypeOfThingReturned();
        final String apiPathPrefix = runtime.apiConfig().getApiEndPointPrefix();
        final String routeViewName =
                runtime.apiSpec()
                        .ruleFor(verb, url, apiPathPrefix)
                        .map(rule -> rule.responseEntityViewFor(response.getStatusCode()))
                        .orElse(null);
        if (routeViewName != null) {
            if (entity.hasViewNamed(routeViewName)) {
                response.usingEntityView(entity.getViewNamed(routeViewName));
            }
            return;
        }

        if (response.hasResponseView()) {
            return;
        }

        if (runtime.apiSpec().defaultResponseEntityViewFor(entity).isPresent()) {
            response.usingEntityResponseViewResolver(defaultResponseViewResolver());
        }
    }

    /**
     * Creates a resolver that asks the API spec for the default response view of each entity.
     *
     * @return resolver used by serializers when entity-level defaults apply
     */
    private EntityResponseViewResolver defaultResponseViewResolver() {
        return entity -> {
            if (entity == null) {
                return null;
            }
            return runtime.apiSpec()
                    .defaultResponseEntityViewFor(entity)
                    .filter(entity::hasViewNamed)
                    .map(entity::getViewNamed)
                    .orElse(null);
        };
    }
}
