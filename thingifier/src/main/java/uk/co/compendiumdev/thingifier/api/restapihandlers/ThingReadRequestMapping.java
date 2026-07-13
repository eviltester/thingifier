package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;

public final class ThingReadRequestMapping {

    private final ThingReadQuery query;
    private final ApiResponse errorResponse;

    private ThingReadRequestMapping(final ThingReadQuery query, final ApiResponse errorResponse) {
        this.query = query;
        this.errorResponse = errorResponse;
    }

    public static ThingReadRequestMapping query(final ThingReadQuery query) {
        return new ThingReadRequestMapping(query, null);
    }

    public static ThingReadRequestMapping error(final ApiResponse errorResponse) {
        return new ThingReadRequestMapping(null, errorResponse);
    }

    public boolean isError() {
        return errorResponse != null;
    }

    public ThingReadQuery getQuery() {
        return query;
    }

    public ApiResponse getErrorResponse() {
        return errorResponse;
    }
}
