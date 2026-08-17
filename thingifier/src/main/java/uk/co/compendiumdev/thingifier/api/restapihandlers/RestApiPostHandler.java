package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.WriteMethodPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Handles generated Thingifier POST routes.
 *
 * <p>The handler maps the route and parsed body to a write command, checks declarative write
 * policy, then delegates lifecycle-aware validation and execution to {@link LifecycleWriteSupport}.
 */
public class RestApiPostHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a POST handler from a Thingifier model.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public RestApiPostHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a POST handler with no lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     */
    public RestApiPostHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a POST handler with lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     * @param lifecycleHooks lifecycle hooks for write processing
     */
    public RestApiPostHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a POST request using a body parser and raw headers.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param requestHeaders request headers used to resolve context
     * @return API response for the POST
     */
    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        return handle(url, args.bodyFields(), runtime.contextFrom(requestHeaders));
    }

    /**
     * Handles a POST request using a body parser and explicit request context.
     *
     * @param url generated API path
     * @param args parsed body source
     * @param context request context containing the active store
     * @return API response for the POST
     */
    public ApiResponse handle(
            final String url, final BodyParser args, final ThingifierRequestContext context) {
        return handle(url, args.bodyFields(), context);
    }

    /**
     * Handles a POST request without lifecycle hook state.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context containing the active store
     * @return API response for the POST
     */
    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return handle(url, bodyFields, context, null);
    }

    /**
     * Handles a POST request with optional lifecycle hook state.
     *
     * @param url generated API path
     * @param bodyFields parsed request body fields
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the POST
     */
    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(url);
        ApiResponse policyResponse =
                new WriteMethodPolicy(runtime)
                        .rejectIfNotAllowed(RoutingVerb.POST, route, bodyFields, context);
        if (policyResponse != null) {
            return policyResponse;
        }

        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(runtime.schema()).mapPost(route, bodyFields);
        return LifecycleWriteSupport.execute(
                runtime,
                lifecycleHooks,
                lifecycle,
                mapping,
                context,
                () ->
                        new ThingWriteRequestMapper(runtime.schema())
                                .mapPost(route, lifecycle.bodyFields()));
    }
}
