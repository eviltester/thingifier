package uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public class NoSuchEntity {

    public static ApiResponse response(final String entityName) {
        return ApiResponse.error404(String.format("No such entity as %s found", entityName));
    }
}
