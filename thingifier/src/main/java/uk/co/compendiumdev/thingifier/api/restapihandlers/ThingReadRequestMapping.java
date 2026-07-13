package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;

public final class ThingReadRequestMapping {

    private final ThingReadQuery query;
    private final ApiMappingError error;

    private ThingReadRequestMapping(final ThingReadQuery query, final ApiMappingError error) {
        this.query = query;
        this.error = error;
    }

    public static ThingReadRequestMapping query(final ThingReadQuery query) {
        return new ThingReadRequestMapping(query, null);
    }

    public static ThingReadRequestMapping error(final ApiMappingError error) {
        return new ThingReadRequestMapping(null, error);
    }

    public boolean isError() {
        return error != null;
    }

    public ThingReadQuery getQuery() {
        return query;
    }

    public ApiMappingError getError() {
        return error;
    }
}
