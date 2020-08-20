package uk.co.compendiumdev.thingifier.application.internalhttpconversion;


import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;

import java.util.*;

final public class ThingifierHttpApiBridge {

    // todo: the methods here are all very similar, we should refactor this commonality

    private final Thingifier thingifier;
    private final ThingifierHttpApi thingifierHttpApi;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;

    public ThingifierHttpApiBridge(final Thingifier aThingifier){
        this(aThingifier, null, null);
    }

    public ThingifierHttpApiBridge(final Thingifier aThingifier,
                                   List<HttpApiRequestHook> apiRequestHooks,
                                   List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;
        if(apiRequestHooks==null){
            this.apiRequestHooks = new ArrayList<>();
        }else{
            this.apiRequestHooks = apiRequestHooks;
        }
        if(apiResponseHooks==null){
            this.apiResponseHooks = new ArrayList<>();
        }else{
            this.apiResponseHooks = apiResponseHooks;
        }

        this.thingifierHttpApi = new ThingifierHttpApi(thingifier,
                                        apiRequestHooks, apiResponseHooks);
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
