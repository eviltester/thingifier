package uk.co.compendiumdev.thingifier.application.query;

import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ReadRelationshipQuery implements ThingReadQuery {

    private final String entityName;
    private final String identifier;
    private final String relationshipName;
    private final QueryFilterParams queryParams;

    public ReadRelationshipQuery(
            final String entityName,
            final String identifier,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        this.entityName = entityName;
        this.identifier = identifier;
        this.relationshipName = relationshipName;
        this.queryParams = ThingReadQuery.copyOf(queryParams);
    }

    @Override
    public String getEntityName() {
        return entityName;
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
