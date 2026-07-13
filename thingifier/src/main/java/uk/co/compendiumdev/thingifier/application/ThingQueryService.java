package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuery;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuerySpec;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingQueryService {

    public RepositoryQueryResult execute(final ThingReadQuery query, final ThingStore store) {
        return new RepositoryQuery(store, specFor(query)).performQuery(query.getQueryParams());
    }

    private RepositoryQuerySpec specFor(final ThingReadQuery query) {
        if (query instanceof ReadRelationshipQuery) {
            ReadRelationshipQuery relationship = (ReadRelationshipQuery) query;
            return RepositoryQuerySpec.relationship(
                    relationship.getEntity(),
                    relationship.getIdentifier(),
                    relationship.getRelationshipName());
        }

        if (query instanceof ReadInstanceQuery) {
            ReadInstanceQuery instance = (ReadInstanceQuery) query;
            return RepositoryQuerySpec.instance(instance.getEntity(), instance.getIdentifier());
        }

        if (query instanceof ReadCollectionQuery) {
            return RepositoryQuerySpec.collection(query.getEntity());
        }

        throw new IllegalArgumentException(
                String.format("Unsupported query %s", query.getClass().getSimpleName()));
    }
}
