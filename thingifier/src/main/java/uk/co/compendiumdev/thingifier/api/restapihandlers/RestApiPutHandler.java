package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiOperationValidationPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.WriteMethodPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Handles generated Thingifier PUT routes.
 *
 * <p>The handler maps PUT into a create or update command according to identifier policy, checks
 * declarative write policy, then delegates lifecycle-aware validation and execution.
 */
public class RestApiPutHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a PUT handler from a Thingifier model.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public RestApiPutHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a PUT handler with no lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     */
    public RestApiPutHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a PUT handler with lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     * @param lifecycleHooks lifecycle hooks for write processing
     */
    public RestApiPutHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a PUT request using a body parser and raw headers.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param requestHeaders request headers used to resolve context
     * @return API response for the PUT
     */
    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        return handle(
                url,
                args.bodyFields(),
                args.rawBody(),
                new QueryFilterParams(),
                runtime.contextFrom(requestHeaders),
                null);
    }

    /**
     * Handles a PUT request using a body parser and explicit request context.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param context request context containing the active store
     * @return API response for the PUT
     */
    public ApiResponse handle(
            final String url, final BodyParser args, final ThingifierRequestContext context) {
        return handle(
                url, args.bodyFields(), args.rawBody(), new QueryFilterParams(), context, null);
    }

    /**
     * Handles a PUT request without lifecycle hook state.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context containing the active store
     * @return API response for the PUT
     */
    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return handle(url, bodyFields, "", new QueryFilterParams(), context, null);
    }

    /**
     * Handles a PUT request with optional lifecycle hook state.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the PUT
     */
    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        return handle(
                url,
                bodyFields,
                lifecycle == null ? "" : lifecycle.rawBody(),
                lifecycle == null ? new QueryFilterParams() : lifecycle.queryParams(),
                context,
                lifecycle);
    }

    /**
     * Handles a PUT request with full request data for operation validation.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param rawBody raw request body text
     * @param queryParams parsed query parameters
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the PUT
     */
    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final String rawBody,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingRoute route = routeFor(url, lifecycle);
        WriteMethodPolicy writePolicy = new WriteMethodPolicy(runtime);
        ApiResponse policyResponse =
                writePolicy.rejectIfNotAllowed(RoutingVerb.PUT, route, bodyFields, context);
        if (policyResponse != null) {
            return policyResponse;
        }

        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(
                                runtime.schema(), runtime.apiConfig().writeMethods().entities())
                        .mapPut(route, bodyFields);
        if (mapping.isError()) {
            return new ThingCommandResultApiMapper(runtime.apiConfig()).map(mapping.getError());
        }

        ApiResponse operationValidationResponse =
                new ApiOperationValidationPolicy(runtime)
                        .rejectIfInvalid(
                                RoutingVerb.PUT,
                                url,
                                route,
                                context,
                                bodyFields,
                                rawBody,
                                queryParams,
                                writePolicy.operationTypeFor(
                                        RoutingVerb.PUT, route, bodyFields, context),
                                lifecycle);
        if (operationValidationResponse != null) {
            return operationValidationResponse;
        }

        return LifecycleWriteSupport.execute(
                runtime,
                lifecycleHooks,
                lifecycle,
                mapping,
                context,
                () ->
                        new ThingWriteRequestMapper(
                                        runtime.schema(),
                                        runtime.apiConfig().writeMethods().entities())
                                .mapPut(route, lifecycle.bodyFields()));
    }

    private ThingRoute routeFor(final String url, final ThingifierApiLifecycleContext lifecycle) {
        return lifecycle == null ? runtime.routeFor(RoutingVerb.PUT, url) : lifecycle.route();
    }
}
