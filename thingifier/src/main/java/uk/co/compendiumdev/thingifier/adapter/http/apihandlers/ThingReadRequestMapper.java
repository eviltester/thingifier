package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ThingReadRequestMapper {

    private final SchemaCatalog schema;

    public ThingReadRequestMapper(final SchemaCatalog schema) {
        this.schema = schema;
    }

    public ThingReadRequestMapping map(
            final ThingRoute route, final QueryFilterParams queryParams) {
        if (route instanceof CollectionRoute) {
            CollectionRoute collection = (CollectionRoute) route;
            return ThingReadRequestMapping.query(
                    new ReadCollectionQuery(collection.entity(), queryParams));
        }

        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            String identifierCandidate = instance.identifier();
            if (schema.hasRelationshipNamed(identifierCandidate)
                    || entityForTerm(identifierCandidate) != null) {
                return notFound(route.originalPath());
            }

            return ThingReadRequestMapping.query(
                    new ReadInstanceQuery(instance.entity(), identifierCandidate, queryParams));
        }

        if (route instanceof RelationshipCollectionRoute) {
            RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            return ThingReadRequestMapping.query(
                    new ReadRelationshipQuery(
                            relationship.parentEntity(),
                            relationship.parentIdentifier(),
                            relationship.relationshipName(),
                            queryParams));
        }

        return notFound(route.originalPath());
    }

    private ThingReadRequestMapping notFound(final String url) {
        return ThingReadRequestMapping.error(
                ApiMappingError.withMessage(
                        404, String.format("Could not find an instance with %s", url)));
    }

    private EntityDefinition entityForTerm(final String term) {
        return schema.definitionWithSingularOrPluralNamed(term);
    }
}
