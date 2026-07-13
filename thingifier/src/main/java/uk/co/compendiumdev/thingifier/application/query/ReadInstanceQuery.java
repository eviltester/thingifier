package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadInstanceQuery implements ThingReadQuery {

    private final EntityDefinition entity;
    private final String identifier;
    private final QueryFilterParams queryParams;

    public ReadInstanceQuery(
            final EntityDefinition entity,
            final String identifier,
            final QueryFilterParams queryParams) {
        this.entity = entity;
        this.identifier = identifier;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public EntityDefinition getEntity() {
        return entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public QueryFilterParams getQueryParams() {
        return ThingReadQuery.copyOf(queryParams);
    }
}
