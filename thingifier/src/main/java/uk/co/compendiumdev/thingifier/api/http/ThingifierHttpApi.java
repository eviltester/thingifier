package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.List;

final public class ThingifierHttpApi {

    // todo: the methods here are all very similar, we should refactor this commonality

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;

    public ThingifierHttpApi(final Thingifier aThingifier){
        this(aThingifier, null, null);
    }

    public ThingifierHttpApi(final Thingifier aThingifier,
                             List<HttpApiRequestHook>apiRequestHooks,
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

        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    public HttpApiResponse get(final HttpApiRequest request) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().get(request.getPath());
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        }

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    public HttpApiResponse delete(final HttpApiRequest request) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().delete(request.getPath());
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        }

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().get(query);
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        }

        return runTheHttpApiResponseHooksOn(httpResponse);

    }

    public HttpApiResponse post(final HttpApiRequest request) {
        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().post(request.getPath(), bodyAsMap(request));
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        }

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().put(request.getPath(), bodyAsMap(request));
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        }

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    private BodyParser bodyAsMap(final HttpApiRequest request) {

        return new BodyParser(request, thingifier.getThingNames());

    }

    private HttpApiResponse runTheHttpApiResponseHooksOn(final HttpApiResponse response) {
        for(HttpApiResponseHook hook : apiResponseHooks){
            HttpApiResponse returnImmediately = hook.run(response);
            if(returnImmediately!=null){
                return returnImmediately;
            }
        }
        return response;
    }

    private HttpApiResponse runTheHttpApiRequestHooksOn(final HttpApiRequest request) {
        for(HttpApiRequestHook hook : apiRequestHooks){
            HttpApiResponse response = hook.run(request);
            if(response!=null){
                return response;
            }
        }
        return null;
    }
}
