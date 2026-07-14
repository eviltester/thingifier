package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadInstanceQuery implements ThingReadQuery {

    private final String entityName;
    private final String identifier;
    private final QueryFilterParams queryParams;

    public ReadInstanceQuery(
            final String entityName, final String identifier, final QueryFilterParams queryParams) {
        this.entityName = entityName;
        this.identifier = identifier;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public QueryFilterParams getQueryParams() {
        return ThingReadQuery.copyOf(queryParams);
    }
}
