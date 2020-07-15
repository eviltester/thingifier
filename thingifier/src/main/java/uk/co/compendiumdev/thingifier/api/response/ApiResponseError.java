package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;

final public class ApiResponseError {

    private ApiResponseError() {
    }

    public static String asAppropriate(final String accept, final String errorMessage) {

        boolean isJson = true; // default to json

        AcceptHeaderParser acceptable = new AcceptHeaderParser(accept);

        // TODO: should be able to configure a default API response type rather than assume it is JSON
        if (acceptable.hasAPreferenceForXml()) {
            isJson = false;
        }

        if (isJson) {
            return ApiResponseAsJson.getErrorMessageJson(errorMessage);
        } else {
            return ApiResponseAsXml.getErrorMessageXml(errorMessage);
        }
    }
}
