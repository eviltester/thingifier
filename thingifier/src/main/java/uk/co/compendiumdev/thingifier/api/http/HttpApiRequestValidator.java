package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headervalidator.AcceptHeaderValidator;
import uk.co.compendiumdev.thingifier.api.http.headers.headervalidator.ContentTypeHeaderValidator;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

import java.util.ArrayList;

public class HttpApiRequestValidator {

    private final ThingifierApiConfig apiConfig;
    Boolean isValid;
    private ApiResponse errorResponse;

    public HttpApiRequestValidator(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public boolean validateSyntax(final HttpApiRequest request, final ThingifierHttpApi.HttpVerb verb) {
        // Config Validation

        ApiResponse apiResponse = new AcceptHeaderValidator(this.apiConfig).
                validate(request.getAcceptHeader());
        ;

        if (apiResponse == null) {
            if (apiConfig.statusCodes().getMaxRequestBodyLengthBytes() > -1) {
                // check the request length
                int maxLengthBytesAllowed = apiConfig.statusCodes().getMaxRequestBodyLengthBytes();
                if (request.getBody() != null && request.getBody().length() > maxLengthBytesAllowed) {
                    apiResponse = ApiResponse.error(
                            413,
                            String.format(
                                    "Error: Request body too large, max allowed is %d bytes",
                                    maxLengthBytesAllowed));
                }
            }
        }

        if (apiResponse == null) {
            // only validate content if it contains content
            if (verb == ThingifierHttpApi.HttpVerb.POST || verb == ThingifierHttpApi.HttpVerb.PUT || verb == ThingifierHttpApi.HttpVerb.PATCH) {

                apiResponse = new ContentTypeHeaderValidator(this.apiConfig).
                        validate(request.getContentTypeHeader());

                // validate the content syntax format against content type
                if (apiResponse == null) {
                    BodyParser parser = new BodyParser(request, new ArrayList<>());
                    String parsingError = "";
                    if(!apiConfig.willAllowJsonAsDefaultContentType()) {
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

    public ApiResponse getErrorApiResponse() {
        return this.errorResponse;
    }
}
