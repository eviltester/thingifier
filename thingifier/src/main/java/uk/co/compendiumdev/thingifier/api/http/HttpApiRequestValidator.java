package uk.co.compendiumdev.thingifier.api.http;

import java.util.ArrayList;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headervalidator.AcceptHeaderValidator;
import uk.co.compendiumdev.thingifier.api.http.headers.headervalidator.ContentTypeHeaderValidator;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class HttpApiRequestValidator {

    private final ThingifierApiConfig apiConfig;
    Boolean isValid;
    private ApiResponse errorResponse;

    public HttpApiRequestValidator(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public boolean validateSyntax(
            final HttpApiRequest request, final ThingifierHttpApi.HttpVerb verb) {
        // Config Validation

        ApiResponse apiResponse =
                new AcceptHeaderValidator(this.apiConfig).validate(request.getAcceptHeader());
        ;

        if (apiResponse == null) {
            if (apiConfig.statusCodes().getMaxRequestBodyLengthBytes() > -1) {
                // check the request length
                int maxLengthBytesAllowed = apiConfig.statusCodes().getMaxRequestBodyLengthBytes();
                if (request.getBody() != null
                        && request.getBody().length() > maxLengthBytesAllowed) {
                    apiResponse =
                            ApiResponse.error(
                                    413,
                                    String.format(
                                            "Error: request body too large, max allowed is %d bytes",
                                            maxLengthBytesAllowed));
                }
            }
        }

        if (apiResponse == null) {
            if (verb == ThingifierHttpApi.HttpVerb.QUERY) {
                apiResponse = validateQueryContent(request);
            }
        }

        if (apiResponse == null) {
            // only validate content if it contains content
            if (verb == ThingifierHttpApi.HttpVerb.POST || verb == ThingifierHttpApi.HttpVerb.PUT) {

                apiResponse =
                        new ContentTypeHeaderValidator(this.apiConfig)
                                .validate(request.getContentTypeHeader());

                // validate the content syntax format against content type
                if (apiResponse == null) {
                    BodyParser parser = new BodyParser(request, new ArrayList<>());
                    String parsingError = "";
                    if (!apiConfig.willAllowJsonAsDefaultContentType()) {
                        parsingError = parser.validBodyBasedOnContentType();
                    }
                    if (!parsingError.isEmpty()) {
                        apiResponse = ApiResponse.error(400, parsingError);
                    }
                }
            }
        }

        this.errorResponse = apiResponse;

        this.isValid = (apiResponse == null);
        return this.isValid;
    }

    private ApiResponse validateQueryContent(final HttpApiRequest request) {
        final ContentTypeHeaderParser contentType =
                new ContentTypeHeaderParser(request.getContentTypeHeader());

        if (contentType.isMissing()) {
            return queryContentError(400, "Missing Content-Type for QUERY request");
        }

        if (!contentType.isFormUrlEncoded()) {
            ApiResponse response =
                    queryContentError(
                            this.apiConfig.statusCodes().contentTypeNotSupported(),
                            "Unsupported QUERY Content Type - " + request.getContentTypeHeader());
            response.setHeader("Accept", ThingifierHttpApi.QUERY_CONTENT_TYPE);
            return response;
        }

        try {
            new UrlQueryParamParser().parseStrict(request.getBody());
        } catch (IllegalArgumentException e) {
            return queryContentError(400, e.getMessage());
        }

        return null;
    }

    private ApiResponse queryContentError(final int statusCode, final String message) {
        return ApiResponse.error(statusCode, message)
                .setHeader(
                        ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                        ThingifierHttpApi.QUERY_CONTENT_TYPE);
    }

    public ApiResponse getErrorApiResponse() {
        return this.errorResponse;
    }
}
