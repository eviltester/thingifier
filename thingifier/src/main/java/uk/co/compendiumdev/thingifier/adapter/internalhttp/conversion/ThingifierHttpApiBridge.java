package uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

/**
 * Bridges the internal HTTP abstraction to the generated Thingifier HTTP API adapter.
 *
 * <p>The bridge is used by the server routing layer so request/response conversion, legacy hooks,
 * and lifecycle hooks all flow into a single {@link ThingifierHttpApi} instance.
 */
public final class ThingifierHttpApiBridge {

    // todo: the methods here are all very similar, we should refactor this commonality

    private final Thingifier thingifier;
    private final ThingifierHttpApi thingifierHttpApi;

    /**
     * Creates a bridge with no HTTP API hooks.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public ThingifierHttpApiBridge(final Thingifier aThingifier) {
        this(aThingifier, (List<HttpApiRequestHook>) null, (List<HttpApiResponseHook>) null);
    }

    /**
     * Creates a bridge using legacy list-based API request and response hooks.
     *
     * @param aThingifier Thingifier model and configuration
     * @param apiRequestHooks hooks run before generated API processing
     * @param apiResponseHooks hooks run after generated API processing
     */
    public ThingifierHttpApiBridge(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;
        this.thingifierHttpApi =
                new ThingifierHttpApi(thingifier, apiRequestHooks, apiResponseHooks);
    }

    /**
     * Creates a bridge using the scoped legacy hook registry.
     *
     * @param aThingifier Thingifier model and configuration
     * @param hookRegistry scoped request/response hook registry
     */
    public ThingifierHttpApiBridge(
            final Thingifier aThingifier, final HttpApiHookRegistry hookRegistry) {
        this(aThingifier, hookRegistry, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a bridge using both scoped legacy hooks and lifecycle hooks.
     *
     * @param aThingifier Thingifier model and configuration
     * @param hookRegistry scoped request/response hook registry
     * @param lifecycleHooks lifecycle hook registry
     * @return configured internal HTTP bridge
     */
    public static ThingifierHttpApiBridge withHookRegistries(
            final Thingifier aThingifier,
            final HttpApiHookRegistry hookRegistry,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        return new ThingifierHttpApiBridge(aThingifier, hookRegistry, lifecycleHooks);
    }

    private ThingifierHttpApiBridge(
            final Thingifier aThingifier,
            final HttpApiHookRegistry hookRegistry,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.thingifier = aThingifier;
        this.thingifierHttpApi =
                ThingifierHttpApi.withHookRegistries(thingifier, hookRegistry, lifecycleHooks);
    }

    /**
     * Handles an internal GET request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse get(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.get(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal HEAD request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse head(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.head(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal POST request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse post(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.post(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal DELETE request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse delete(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.delete(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal PUT request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse put(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.put(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal PATCH request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse patch(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.patch(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles an internal QUERY request.
     *
     * @param theRequest internal HTTP request
     * @return internal HTTP response
     */
    public InternalHttpResponse queryRequest(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.queryRequest(toHttpApiRequest(theRequest)));
    }

    /**
     * Handles the legacy query helper for an internal request.
     *
     * @param theRequest internal HTTP request
     * @param query query expression
     * @return internal HTTP response
     */
    public InternalHttpResponse query(final InternalHttpRequest theRequest, final String query) {
        return toInternalResponse(thingifierHttpApi.query(toHttpApiRequest(theRequest), query));
    }

    /**
     * Converts an internal request to the HTTP API request type.
     *
     * @param theRequest internal HTTP request
     * @return HTTP API request
     */
    private HttpApiRequest toHttpApiRequest(final InternalHttpRequest theRequest) {
        return InternalHttpRequestToHttpApiRequest.convert(theRequest);
    }

    /**
     * Converts an HTTP API response back to the internal response type.
     *
     * @param theResponse HTTP API response
     * @return internal HTTP response
     */
    private InternalHttpResponse toInternalResponse(final HttpApiResponse theResponse) {
        return HttpApiResponseToInternalHttpResponse.convert(theResponse);
    }
}
