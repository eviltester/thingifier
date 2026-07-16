package uk.co.compendiumdev.challenge.httpserver;

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
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class HttpMessageLengthValidator {

    public static final int DEFAULT_MAX_LENGTH = 24000;

    private final int maxLength;

    public HttpMessageLengthValidator() {
        this(DEFAULT_MAX_LENGTH);
    }

    HttpMessageLengthValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean rejectRequestTooLong(
            final HttpServerRequest request, final HttpServerResponse result) {
        if (contentLength(request) > this.maxLength) {
            // randomly picked 24K
            result.status(413);
            return true;
        }
        return false;
    }

    public String messageTooLongErrorResponse(
            final ThingifierApiConfig apiConfig,
            final HttpServerRequest request,
            final HttpServerResponse result) {
        final ApiResponse response =
                ApiResponse.error(
                        413,
                        String.format(
                                "Error: request too large, max allowed is %d bytes",
                                this.maxLength));

        final InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
        final HttpApiRequest myRequest =
                InternalHttpRequestToHttpApiRequest.convert(internalRequest);
        JsonThing jsonThing = new JsonThing(apiConfig.jsonOutput());
        final HttpApiResponse httpApiResponse =
                new HttpApiResponse(myRequest.getHeaders(), response, jsonThing, apiConfig);

        return InternalHttpResponseToHttpServer.convert(
                HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);
    }

    private int contentLength(final HttpServerRequest request) {
        try {
            return Integer.parseInt(request.contentLength());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
