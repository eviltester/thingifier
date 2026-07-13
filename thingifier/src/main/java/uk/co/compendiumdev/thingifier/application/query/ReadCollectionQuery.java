package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadCollectionQuery implements ThingReadQuery {

    private final EntityDefinition entity;
    private final QueryFilterParams queryParams;

    public ReadCollectionQuery(final EntityDefinition entity, final QueryFilterParams queryParams) {
        this.entity = entity;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public EntityDefinition getEntity() {
        return entity;
    }

    @Override
    public QueryFilterParams getQueryParams() {
        return ThingReadQuery.copyOf(queryParams);
    }
}
