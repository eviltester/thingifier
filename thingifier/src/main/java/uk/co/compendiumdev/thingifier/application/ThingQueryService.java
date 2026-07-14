package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuery;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuerySpec;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingQueryService {

    private final SchemaDefinitionResolver schema;

    public ThingQueryService(final SchemaDefinitionResolver schema) {
        this.schema = schema;
    }

    public RepositoryQueryResult execute(final ThingReadQuery query, final ThingStore store) {
        return new RepositoryQuery(store, specFor(query)).performQuery(query.getQueryParams());
    }

    private RepositoryQuerySpec specFor(final ThingReadQuery query) {
        EntityDefinition entity = schema.entityNamed(query.getEntityName());
        if (query instanceof ReadRelationshipQuery) {
            ReadRelationshipQuery relationship = (ReadRelationshipQuery) query;
            return RepositoryQuerySpec.relationship(
                    entity, relationship.getIdentifier(), relationship.getRelationshipName());
        }

        if (query instanceof ReadInstanceQuery) {
            ReadInstanceQuery instance = (ReadInstanceQuery) query;
            return RepositoryQuerySpec.instance(entity, instance.getIdentifier());
        }

        if (query instanceof ReadCollectionQuery) {
            return RepositoryQuerySpec.collection(entity);
        }

        throw new IllegalArgumentException(
                String.format("Unsupported query %s", query.getClass().getSimpleName()));
    }
}
