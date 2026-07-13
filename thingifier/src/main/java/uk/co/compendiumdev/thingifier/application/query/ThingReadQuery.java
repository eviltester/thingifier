package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public interface ThingReadQuery {

    EntityDefinition getEntity();

    QueryFilterParams getQueryParams();

    static QueryFilterParams copyOf(final QueryFilterParams queryParams) {
        QueryFilterParams copy = new QueryFilterParams();
        if (queryParams == null) {
            return copy;
        }

        for (FilterBy filterBy : queryParams.toList()) {
            copy.put(filterBy.fieldName, filterBy.filterOperation + filterBy.fieldValue);
        }
        return copy;
    }
}
