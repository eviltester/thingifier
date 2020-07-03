package uk.co.compendiumdev.thingifier.api.response;

final public class ApiResponseError {

    private ApiResponseError() {
    }

    public static String asAppropriate(final String accept, final String errorMessage) {

        boolean isJson = true; // default to json

        if (accept != null && accept.endsWith("/xml")) {
            isJson = false;
        }

        if (isJson) {
            return ApiResponseAsJson.getErrorMessageJson(errorMessage);
        } else {
            return ApiResponseAsXml.getErrorMessageXml(errorMessage);
        }
    }
}
