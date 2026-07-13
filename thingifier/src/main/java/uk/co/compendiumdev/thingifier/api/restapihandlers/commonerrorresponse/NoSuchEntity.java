package uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse;

import uk.co.compendiumdev.thingifier.api.restapihandlers.ApiMappingError;

public class NoSuchEntity {

    public static ApiMappingError error(final String entityName) {
        return ApiMappingError.withMessage(
                404, String.format("No such entity as %s found", entityName));
    }
}
