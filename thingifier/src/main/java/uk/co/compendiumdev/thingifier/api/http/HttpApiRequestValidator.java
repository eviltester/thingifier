package uk.co.compendiumdev.thingifier.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
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
            if (verb == ThingifierHttpApi.HttpVerb.POST
                    || verb == ThingifierHttpApi.HttpVerb.PUT
                    || verb == ThingifierHttpApi.HttpVerb.PATCH) {

                apiResponse = validateWriteContentType(request, verb);

                // validate the content syntax format against content type
                if (apiResponse == null
                        && (verb == ThingifierHttpApi.HttpVerb.POST
                                || verb == ThingifierHttpApi.HttpVerb.PUT)) {
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

    private ApiResponse validateWriteContentType(
            final HttpApiRequest request, final ThingifierHttpApi.HttpVerb verb) {
        final ContentTypeHeaderParser contentType =
                new ContentTypeHeaderParser(request.getContentTypeHeader());
        if (verb == ThingifierHttpApi.HttpVerb.PATCH
                && (contentType.isJsonMergePatch() || contentType.isJsonPatch())) {
            return null;
        }

        return new ContentTypeHeaderValidator(this.apiConfig)
                .validate(request.getContentTypeHeader());
    }

    private ApiResponse validateQueryContent(final HttpApiRequest request) {
        final ContentTypeHeaderParser contentType =
                new ContentTypeHeaderParser(request.getContentTypeHeader());

        if (contentType.isMissing()) {
            return queryContentError(400, "Missing Content-Type for QUERY request");
        }

        if (!contentType.isFormUrlEncoded()
                && !contentType.isJsonPath()
                && !contentType.isStructuredTodoQueryJson()) {
            ApiResponse response =
                    queryContentError(
                            this.apiConfig.statusCodes().contentTypeNotSupported(),
                            "Unsupported QUERY Content Type - " + request.getContentTypeHeader());
            response.setHeader("Accept", ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
            return response;
        }

        try {
            if (contentType.isJsonPath()) {
                validateJsonPath(request.getBody());
            } else if (contentType.isStructuredTodoQueryJson()) {
                validateStructuredQueryJson(request.getBody());
            } else {
                new UrlQueryParamParser().parseStrict(request.getBody());
            }
        } catch (IllegalArgumentException | InvalidPathException | JsonProcessingException e) {
            return queryContentError(400, e.getMessage());
        }

        return null;
    }

    private void validateJsonPath(final String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing JSONPath expression for QUERY request");
        }
        JsonPath.compile(body.trim());
    }

    private void validateStructuredQueryJson(final String body) throws JsonProcessingException {
        JsonBodyValueConverter.readStrictTree(body);
    }

    private ApiResponse queryContentError(final int statusCode, final String message) {
        return ApiResponse.error(statusCode, message)
                .setHeader(
                        ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                        ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
    }

    public ApiResponse getErrorApiResponse() {
        return this.errorResponse;
    }
}
