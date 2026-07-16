package uk.co.compendiumdev.thingifier.adapter.httpserver.routehandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.HttpApiResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.InternalHttpRequestToHttpApiRequest;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public class HttpApiRequestResponseHandler {
    private final HttpServerRequest request;
    private final HttpServerResponse response;
    private final Thingifier thingifier;
    private final ThingifierHttpApi httpApi;
    private HttpApiRequestHandler handler;
    private boolean validate = true; // validate message by default

    public HttpApiRequestResponseHandler(
            final HttpServerRequest request,
            final HttpServerResponse result,
            final Thingifier thingifier) {
        this.request = request;
        this.response = result;
        this.thingifier = thingifier;
        this.httpApi = new ThingifierHttpApi(thingifier);
    }

    public HttpApiRequestResponseHandler usingHandler(final HttpApiRequestHandler handler) {
        this.handler = handler;
        return this;
    }

    public HttpApiRequestResponseHandler validateRequestSyntax(boolean shouldValidate) {
        this.validate = shouldValidate;
        return this;
    }

    public String handle() {

        final InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
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

        return InternalHttpResponseToHttpServer.convert(
                HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), response);
    }
}
