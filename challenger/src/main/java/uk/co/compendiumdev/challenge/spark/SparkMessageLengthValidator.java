package uk.co.compendiumdev.challenge.spark;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.HttpApiResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpRequestToHttpApiRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponseToSpark;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.SparkToInternalHttpRequest;

public class SparkMessageLengthValidator {

    public static final int DEFAULT_MAX_LENGTH = 24000;

    private final int maxLength;

    public SparkMessageLengthValidator() {
        this(DEFAULT_MAX_LENGTH);
    }

    SparkMessageLengthValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean rejectRequestTooLong(final Request request, final Response result) {
        if (request.contentLength() > this.maxLength) {
            // randomly picked 24K
            result.status(413);
            return true;
        }
        return false;
    }

    public String messageTooLongErrorResponse(
            final ThingifierApiConfig apiConfig, final Request request, final Response result) {
        final ApiResponse response =
                ApiResponse.error(
                        413,
                        String.format(
                                "Error: Request too large, max allowed is %d bytes",
                                this.maxLength));

        final InternalHttpRequest internalRequest = SparkToInternalHttpRequest.convert(request);
        final HttpApiRequest myRequest =
                InternalHttpRequestToHttpApiRequest.convert(internalRequest);
        JsonThing jsonThing = new JsonThing(apiConfig.jsonOutput());
        final HttpApiResponse httpApiResponse =
                new HttpApiResponse(myRequest.getHeaders(), response, jsonThing, apiConfig);

        return InternalHttpResponseToSpark.convert(
                HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);
    }
}
