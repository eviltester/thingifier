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
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Handles generated Thingifier DELETE routes.
 *
 * <p>The handler maps entity and relationship deletes to write commands, checks declarative write
 * policy, then delegates lifecycle-aware validation and execution.
 */
public class RestApiDeleteHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a DELETE handler from a Thingifier model.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a DELETE handler with no lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     */
    public RestApiDeleteHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a DELETE handler with lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     * @param lifecycleHooks lifecycle hooks for write processing
     */
    public RestApiDeleteHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a DELETE request using raw headers to resolve context.
     *
     * @param url generated API path
     * @param requestHeaders request headers used to resolve context
     * @return API response for the DELETE
     */
    public ApiResponse handle(final String url, HttpHeadersBlock requestHeaders) {
        return handle(url, runtime.contextFrom(requestHeaders));
    }

    /**
     * Handles a DELETE request without lifecycle hook state.
     *
     * @param url generated API path
     * @param context request context containing the active store
     * @return API response for the DELETE
     */
    public ApiResponse handle(final String url, final ThingifierRequestContext context) {
        return handle(url, new QueryFilterParams(), context, null);
    }

    /**
     * Handles a DELETE request with optional lifecycle hook state.
     *
     * @param url generated API path
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the DELETE
     */
    public ApiResponse handle(
            final String url,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        return handle(
                url,
                lifecycle == null ? new QueryFilterParams() : lifecycle.queryParams(),
                context,
                lifecycle);
    }

    /**
     * Handles a DELETE request with full request data for operation validation.
     *
     * @param url generated API path
     * @param queryParams parsed query parameters
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the DELETE
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingRoute route = routeFor(url, lifecycle);
        WriteMethodPolicy writePolicy = new WriteMethodPolicy(runtime);
        ApiResponse policyResponse =
                writePolicy.rejectIfNotAllowed(
                        RoutingVerb.DELETE, route, ApiBodyFields.empty(), context);
        if (policyResponse != null) {
            return policyResponse;
        }

        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(runtime.schema()).mapDelete(route);
        if (mapping.isError()) {
            return new ThingCommandResultApiMapper(runtime.apiConfig()).map(mapping.getError());
        }

        ApiResponse operationValidationResponse =
                new ApiOperationValidationPolicy(runtime)
                        .rejectIfInvalid(
                                RoutingVerb.DELETE,
                                url,
                                route,
                                context,
                                ApiBodyFields.empty(),
                                "",
                                queryParams,
                                writePolicy.operationTypeFor(
                                        RoutingVerb.DELETE, route, ApiBodyFields.empty(), context),
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
                () -> new ThingWriteRequestMapper(runtime.schema()).mapDelete(route));
    }

    private ThingRoute routeFor(final String url, final ThingifierApiLifecycleContext lifecycle) {
        return lifecycle == null ? runtime.routeFor(RoutingVerb.DELETE, url) : lifecycle.route();
    }
}
