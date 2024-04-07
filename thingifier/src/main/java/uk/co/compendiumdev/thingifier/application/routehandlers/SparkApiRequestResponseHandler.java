package uk.co.compendiumdev.thingifier.application.routehandlers;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.HttpApiResponseToSpark;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.SparkToHttpApiRequest;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;

public class SparkApiRequestResponseHandler {
    private final Request request;
    private final Response response;
    private final Thingifier thingifier;
    private final ThingifierHttpApi httpApi;
    private HttpApiRequestHandler handler;
    private boolean validate=true; // validate message by default

    public SparkApiRequestResponseHandler(final Request request,
                                          final Response result,
                                          final Thingifier thingifier) {
        this.request = request;
        this.response = result;
        this.thingifier = thingifier;
        this.httpApi = new ThingifierHttpApi(thingifier);
    }

    public SparkApiRequestResponseHandler usingHandler(final HttpApiRequestHandler handler) {
        this.handler = handler;
        return this;
    }

    public SparkApiRequestResponseHandler validateRequestSyntax(boolean shouldValidate){
        this.validate = shouldValidate;
        return this;
    }

    public String handle(){

        final HttpApiRequest myRequest = SparkToHttpApiRequest.convert(request);

        final JsonThing jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());

        ApiResponse apiResponse = null;

        // handle input validation - e.g. mirror/raw should not validate request
        HttpApiResponse httpApiResponse = null;
        if(validate) {
            httpApiResponse = httpApi.validateRequestSyntax(myRequest,
                    ThingifierHttpApi.HttpVerb.GET);
        }

        if(httpApiResponse == null) {
            apiResponse = handler.handle(myRequest);

            httpApiResponse = new HttpApiResponse(myRequest.getHeaders(), apiResponse,
                    jsonThing, thingifier.apiConfig());
        }

        return HttpApiResponseToSpark.convert(httpApiResponse, response);
    }
}
