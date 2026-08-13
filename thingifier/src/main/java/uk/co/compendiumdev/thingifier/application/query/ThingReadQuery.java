package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public interface ThingReadQuery {

    String getEntityName();

    QueryFilterParams getQueryParams();

    static QueryFilterParams copyOf(final QueryFilterParams queryParams) {
        QueryFilterParams copy = new QueryFilterParams();
        if (queryParams == null) {
            return copy;
        }

        for (FilterBy filterBy : queryParams.toList()) {
            copy.add(filterBy.copy());
        }
        return copy;
    }
}
