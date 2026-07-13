package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadRelationshipQuery implements ThingReadQuery {

    private final EntityDefinition entity;
    private final String identifier;
    private final String relationshipName;
    private final QueryFilterParams queryParams;

    public ReadRelationshipQuery(
            final EntityDefinition entity,
            final String identifier,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        this.entity = entity;
        this.identifier = identifier;
        this.relationshipName = relationshipName;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public EntityDefinition getEntity() {
        return entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    @Override
    public QueryFilterParams getQueryParams() {
        return ThingReadQuery.copyOf(queryParams);
    }
}
