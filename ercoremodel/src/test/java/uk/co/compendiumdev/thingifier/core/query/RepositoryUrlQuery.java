package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class RepositoryUrlQuery implements RepositoryQueryResult {

    private final ERSchema schema;
    private final ThingStore store;
    private final String route;
    private RepositoryQuery queryResults;

    public RepositoryUrlQuery(final ERSchema schema, final ThingStore store, final String route) {
        this.schema = schema;
        this.store = store;
        this.route = route;
    }

    public static boolean canHandle(final ERSchema schema, final String route) {
        String[] terms = termsFrom(route);
        if (terms.length == 0 || terms.length > 3) {
            return false;
        }

        EntityDefinition entity = entityForTerm(schema, terms[0]);
        if (entity == null) {
            return false;
        }

        if (terms.length == 1) {
            return true;
        }

        String identifierCandidate = terms[1];
        if (schema.hasRelationshipNamed(identifierCandidate)
                || entityForTerm(schema, identifierCandidate) != null) {
            return false;
        }

        if (terms.length == 2) {
            return true;
        }

        return entity.related().hasRelationship(terms[2]);
    }

    public RepositoryUrlQuery performQuery() {
        return performQuery(new QueryFilterParams());
    }

    public RepositoryUrlQuery performQuery(final QueryFilterParams queryParams) {
        queryResults = new RepositoryQuery(store, specFromRoute()).performQuery(queryParams);
        return this;
    }

    @Override
    public boolean wasQueryIntendedToMatchAnInstance() {
        return queryResults().wasQueryIntendedToMatchAnInstance();
    }

    @Override
    public boolean isResultACollection() {
        return queryResults().isResultACollection();
    }

    @Override
    public List<EntityInstance> getListEntityInstances() {
        return new ArrayList<>(queryResults().getListEntityInstances());
    }

    @Override
    public EntityInstance getLastInstance() {
        return queryResults().getLastInstance();
    }

    @Override
    public boolean lastMatchWasInstance() {
        return queryResults().lastMatchWasInstance();
    }

    @Override
    public boolean lastMatchWasNothing() {
        return queryResults().lastMatchWasNothing();
    }

    @Override
    public EntityDefinition resultContainsDefn() {
        return queryResults().resultContainsDefn();
    }

    private RepositoryQuery queryResults() {
        if (queryResults == null) {
            throw new IllegalStateException("Query has not been performed");
        }
        return queryResults;
    }

    private RepositoryQuerySpec specFromRoute() {
        String[] terms = termsFrom(route);
        EntityDefinition entity = entityForTerm(schema, terms[0]);
        if (terms.length == 1) {
            return RepositoryQuerySpec.collection(entity);
        }
        if (terms.length == 2) {
            return RepositoryQuerySpec.instance(entity, terms[1]);
        }
        return RepositoryQuerySpec.relationship(entity, terms[1], terms[2]);
    }

    private static EntityDefinition entityForTerm(final ERSchema schema, final String term) {
        if (schema.hasEntityNamed(term)) {
            return schema.getEntityDefinitionNamed(term);
        }
        if (schema.hasEntityWithPluralNamed(term)) {
            return schema.getEntityDefinitionWithPluralNamed(term);
        }
        return null;
    }

    private static String[] termsFrom(final String route) {
        String normalized = route == null ? "" : route.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isEmpty()) {
            return new String[0];
        }
        return normalized.split("/");
    }
}
