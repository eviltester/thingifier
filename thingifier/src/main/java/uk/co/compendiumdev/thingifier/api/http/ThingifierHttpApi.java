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

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;



    private enum HttpVerb{
        GET, DELETE, POST, PUT, HEAD,
        // NOT Handled
        OPTIONS, PATCH,
    };

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


    private HttpApiResponse requestWrapper(final HttpApiRequest request, HttpVerb verb){

        String acceptHeader = request.getHeader("Accept", "");

        // Config Validation

        ApiResponse apiResponse=null;

        final AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        int statusAcceptTypeNotSupported = thingifier.apiConfig().statusCodes().acceptTypeNotSupported();

        if(thingifier.apiConfig().willApiEnforceAcceptHeaderForResponses()){
            if (!accept.willAcceptXml() ||
                !accept.willAcceptJson()){
                apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "Unrecognised Accept Type");
            }
        }
        boolean willOnlyAcceptXML = accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML) &&
                                    !accept.willAcceptJson();
        if (    willOnlyAcceptXML &&
                !thingifier.apiConfig().willApiAllowXmlForResponses() &&
                thingifier.apiConfig().willApiEnforceAcceptHeaderForResponses()
        ) {
            apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "XML not supported");
        }

        boolean willOnlyAcceptJSON = accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON) &&
                                    !accept.willAcceptXml();
        if (    willOnlyAcceptJSON &&
                !thingifier.apiConfig().willApiAllowJsonForResponses() &&
                thingifier.apiConfig().willApiEnforceAcceptHeaderForResponses()
        ) {
            apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "JSON not supported");
        }

        if(apiResponse!=null){
            return new HttpApiResponse(request.getHeaders(), apiResponse,
                    jsonThing, thingifier.apiConfig());
        }


        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse!=null){
            return httpResponse;
        }

        apiResponse = routeRequest(request, verb);

        httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse,
                jsonThing, thingifier.apiConfig());

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    public ApiResponse routeRequest(final HttpApiRequest request,
                                        HttpVerb verb) {

        ApiResponse apiResponse=null;

        switch (verb){
            case GET:
                apiResponse = thingifier.api().get(request.getPath(),
                                            request.getQueryParams());
                break;
            case HEAD:
                apiResponse = thingifier.api().head(request.getPath());
                break;
            case DELETE:
                apiResponse = thingifier.api().delete(request.getPath());
                break;
            case POST:
                apiResponse = thingifier.api().post(request.getPath(), bodyAsMap(request));
                break;
            case PUT:
                apiResponse = thingifier.api().put(request.getPath(), bodyAsMap(request));
                break;
        }

        return apiResponse;

    }

    public HttpApiResponse get(final HttpApiRequest request) {
        return requestWrapper(request, HttpVerb.GET);
    }

    public HttpApiResponse head(final HttpApiRequest request) {
        return requestWrapper(request, HttpVerb.HEAD);
    }

    public HttpApiResponse delete(final HttpApiRequest request) {
        return requestWrapper(request, HttpVerb.DELETE);
    }

    public HttpApiResponse post(final HttpApiRequest request) {
        return requestWrapper(request, HttpVerb.POST);
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        return requestWrapper(request, HttpVerb.PUT);
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if(httpResponse==null) {
            ApiResponse apiResponse = thingifier.api().get(query);
            httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse,
                    jsonThing, thingifier.apiConfig());
        }

        return runTheHttpApiResponseHooksOn(httpResponse);
    }

    private BodyParser bodyAsMap(final HttpApiRequest request) {

        return new BodyParser(request, thingifier.getThingNames());

    }

    private HttpApiResponse runTheHttpApiResponseHooksOn(final HttpApiResponse response) {
        for(HttpApiResponseHook hook : apiResponseHooks){
            HttpApiResponse returnImmediately = hook.run(response, thingifier.apiConfig());
            if(returnImmediately!=null){
                return returnImmediately;
            }
        }
        return response;
    }

    private HttpApiResponse runTheHttpApiRequestHooksOn(final HttpApiRequest request) {
        for(HttpApiRequestHook hook : apiRequestHooks){
            HttpApiResponse response = hook.run(request, thingifier.apiConfig());
            if(response!=null){
                return response;
            }
        }
        return null;
    }
}
