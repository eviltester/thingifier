package uk.co.compendiumdev.thingifier.api.response;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

public final class ApiResponseError {

    private ApiResponseError() {}

    public static String asAppropriate(final String accept, final String errorMessage) {

        boolean isJson = true; // default to json

        final AcceptHeaderParser acceptable = new AcceptHeaderParser(accept);
        final AcceptHeaderParser.ACCEPT_TYPE preferredType =
                acceptable.preferredSupportedType(
                        List.of(
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                AcceptHeaderParser.ACCEPT_TYPE.XML,
                                AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML),
                        AcceptHeaderParser.ACCEPT_TYPE.JSON);

        // TODO: should be able to configure a default API response type rather than assume it is
        // JSON
        if (preferredType.rendersAsXml()) {
            isJson = false;
        }

        if (isJson) {
            return ApiResponseAsJson.getErrorMessageJson(errorMessage);
        } else {
            return ApiResponseAsXml.getErrorMessageXml(errorMessage);
        }
    }
}
