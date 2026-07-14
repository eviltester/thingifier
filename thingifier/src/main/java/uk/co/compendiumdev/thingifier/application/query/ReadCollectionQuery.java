package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadCollectionQuery implements ThingReadQuery {

    private final String entityName;
    private final QueryFilterParams queryParams;

    public ReadCollectionQuery(final String entityName, final QueryFilterParams queryParams) {
        this.entityName = entityName;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public QueryFilterParams getQueryParams() {
        return ThingReadQuery.copyOf(queryParams);
    }
}
