package uk.co.compendiumdev.thingifier.adapter.spark.routehandlers;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.HttpApiResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.InternalHttpRequestToHttpApiRequest;
import uk.co.compendiumdev.thingifier.adapter.spark.conversion.InternalHttpResponseToSpark;
import uk.co.compendiumdev.thingifier.adapter.spark.conversion.SparkToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public class SparkApiRequestResponseHandler {
    private final Request request;
    private final Response response;
    private final Thingifier thingifier;
    private final ThingifierHttpApi httpApi;
    private HttpApiRequestHandler handler;
    private boolean validate = true; // validate message by default

    public SparkApiRequestResponseHandler(
            final Request request, final Response result, final Thingifier thingifier) {
        this.request = request;
        this.response = result;
        this.thingifier = thingifier;
        this.httpApi = new ThingifierHttpApi(thingifier);
    }

    public SparkApiRequestResponseHandler usingHandler(final HttpApiRequestHandler handler) {
        this.handler = handler;
        return this;
    }

    public SparkApiRequestResponseHandler validateRequestSyntax(boolean shouldValidate) {
        this.validate = shouldValidate;
        return this;
    }

    public String handle() {

        final InternalHttpRequest internalRequest = SparkToInternalHttpRequest.convert(request);
        final HttpApiRequest myRequest =
                InternalHttpRequestToHttpApiRequest.convert(internalRequest);

        final JsonThing jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());

        // handle input validation - e.g. mirror/raw should not validate request
        HttpApiResponse httpApiResponse = null;
        if (validate) {
            httpApiResponse =
                    httpApi.validateRequestSyntax(myRequest, ThingifierHttpApi.HttpVerb.GET);
        }

        if (httpApiResponse == null) {
            ApiResponse apiResponse = handler.handle(myRequest);
            ThingifierRequestContext context =
                    ThingifierRequestContext.from(thingifier, myRequest.getHeaders());
            apiResponse.usingRelationships(context.store().relationships());

            httpApiResponse =
                    new HttpApiResponse(
                            myRequest.getHeaders(), apiResponse, jsonThing, thingifier.apiConfig());
        }

        return InternalHttpResponseToSpark.convert(
                HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), response);
    }
}
