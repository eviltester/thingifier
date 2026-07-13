package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ThingReadRequestMapper {

    private final Thingifier thingifier;

    public ThingReadRequestMapper(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ThingReadRequestMapping map(final String url, final QueryFilterParams queryParams) {
        String[] terms = EntityUrlMatcher.parts(url);
        if (terms.length == 0 || terms.length > 3) {
            return notFound(url);
        }

        EntityDefinition entity = entityForTerm(terms[0]);
        if (entity == null) {
            return notFound(url);
        }

        if (terms.length == 1) {
            return ThingReadRequestMapping.query(new ReadCollectionQuery(entity, queryParams));
        }

        String identifierCandidate = terms[1];
        if (thingifier.getERmodel().getSchema().hasRelationshipNamed(identifierCandidate)
                || entityForTerm(identifierCandidate) != null) {
            return notFound(url);
        }

        if (terms.length == 2) {
            return ThingReadRequestMapping.query(
                    new ReadInstanceQuery(entity, identifierCandidate, queryParams));
        }

        if (!entity.related().hasRelationship(terms[2])) {
            return notFound(url);
        }

        return ThingReadRequestMapping.query(
                new ReadRelationshipQuery(entity, identifierCandidate, terms[2], queryParams));
    }

    private ThingReadRequestMapping notFound(final String url) {
        return ThingReadRequestMapping.error(
                ApiResponse.error404(String.format("Could not find an instance with %s", url)));
    }

    private EntityDefinition entityForTerm(final String term) {
        return thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(term);
    }
}
