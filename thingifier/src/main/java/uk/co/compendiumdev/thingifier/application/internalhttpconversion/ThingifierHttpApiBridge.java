package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;

public final class ThingifierHttpApiBridge {

    // todo: the methods here are all very similar, we should refactor this commonality

    private final Thingifier thingifier;
    private final ThingifierHttpApi thingifierHttpApi;

    public ThingifierHttpApiBridge(final Thingifier aThingifier) {
        this(aThingifier, null, null);
    }

    public ThingifierHttpApiBridge(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;
        this.thingifierHttpApi =
                new ThingifierHttpApi(thingifier, apiRequestHooks, apiResponseHooks);
    }

    public HttpApiResponse get(final HttpApiRequest theRequest) {
        return thingifierHttpApi.get(theRequest);
    }

    public HttpApiResponse head(final HttpApiRequest theRequest) {
        return thingifierHttpApi.head(theRequest);
    }

    public HttpApiResponse post(final HttpApiRequest theRequest) {
        return thingifierHttpApi.post(theRequest);
    }

    public HttpApiResponse delete(final HttpApiRequest theRequest) {
        return thingifierHttpApi.delete(theRequest);
    }

    public HttpApiResponse put(final HttpApiRequest theRequest) {
        return thingifierHttpApi.put(theRequest);
    }

    public HttpApiResponse query(final HttpApiRequest theRequest, final String query) {
        return thingifierHttpApi.query(theRequest, query);
    }
}
