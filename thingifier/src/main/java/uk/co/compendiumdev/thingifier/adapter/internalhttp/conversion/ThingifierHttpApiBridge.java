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

public final class ThingifierHttpApiBridge {

    // todo: the methods here are all very similar, we should refactor this commonality

    private final Thingifier thingifier;
    private final ThingifierHttpApi thingifierHttpApi;

    public ThingifierHttpApiBridge(final Thingifier aThingifier) {
        this(aThingifier, (List<HttpApiRequestHook>) null, (List<HttpApiResponseHook>) null);
    }

    public ThingifierHttpApiBridge(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;
        this.thingifierHttpApi =
                new ThingifierHttpApi(thingifier, apiRequestHooks, apiResponseHooks);
    }

    public ThingifierHttpApiBridge(
            final Thingifier aThingifier, final HttpApiHookRegistry hookRegistry) {
        this(aThingifier, hookRegistry, new ThingifierApiLifecycleHookRegistry());
    }

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

    public InternalHttpResponse get(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.get(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse head(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.head(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse post(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.post(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse delete(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.delete(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse put(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.put(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse patch(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.patch(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse queryRequest(final InternalHttpRequest theRequest) {
        return toInternalResponse(thingifierHttpApi.queryRequest(toHttpApiRequest(theRequest)));
    }

    public InternalHttpResponse query(final InternalHttpRequest theRequest, final String query) {
        return toInternalResponse(thingifierHttpApi.query(toHttpApiRequest(theRequest), query));
    }

    private HttpApiRequest toHttpApiRequest(final InternalHttpRequest theRequest) {
        return InternalHttpRequestToHttpApiRequest.convert(theRequest);
    }

    private InternalHttpResponse toInternalResponse(final HttpApiResponse theResponse) {
        return HttpApiResponseToInternalHttpResponse.convert(theResponse);
    }
}
