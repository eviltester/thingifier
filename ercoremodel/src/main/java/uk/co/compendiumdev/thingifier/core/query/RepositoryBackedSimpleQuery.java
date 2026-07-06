package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryBackedSimpleQuery implements QueryResult {

    private final ERSchema schema;
    private final ThingRepository repository;
    private final String query;

    private boolean isCollection;
    private boolean wasIntentToMatchInstance;
    private boolean lastMatchWasInstance;
    private boolean lastMatchWasNothing = true;
    private EntityDefinition resultContainsDefinition;
    private EntityInstance currentInstance;
    private List<EntityInstance> foundItems = new ArrayList<>();

    public RepositoryBackedSimpleQuery(
            final ERSchema schema,
            final ThingRepository repository,
            final String query) {
        this.schema = schema;
        this.repository = repository;
        if (query.startsWith("/")) {
            this.query = query.substring(1);
        } else {
            this.query = query;
        }
    }

    public static boolean canHandle(final ERSchema schema, final String query) {
        String[] terms = termsFrom(query);
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
        if (schema.hasRelationshipNamed(identifierCandidate) ||
                entityForTerm(schema, identifierCandidate) != null) {
            return false;
        }

        if (terms.length == 2) {
            return true;
        }

        return entity.related().hasRelationship(terms[2]);
    }

    public RepositoryBackedSimpleQuery performQuery(final QueryFilterParams queryParams) {
        String[] terms = termsFrom(query);
        EntityDefinition entity = entityForTerm(schema, terms[0]);
        resultContainsDefinition = entity;

        if (terms.length == 1) {
            isCollection = true;
            foundItems = new ArrayList<>(repository.listInstances(entity, queryParams));
            lastMatchWasNothing = false;
            return this;
        }

        wasIntentToMatchInstance = true;
        currentInstance = repository.findInstanceByQueryIdentifier(entity, terms[1]);
        if (currentInstance == null) {
            foundItems = new ArrayList<>();
            lastMatchWasNothing = true;
            lastMatchWasInstance = false;
            return this;
        }

        if (terms.length == 3) {
            wasIntentToMatchInstance = true;
            isCollection = true;
            foundItems = new ArrayList<>(
                    repository.listRelatedInstances(currentInstance, terms[2], queryParams));
            resultContainsDefinition = relatedEntityFor(currentInstance, terms[2]);
            lastMatchWasNothing = false;
            lastMatchWasInstance = false;
            return this;
        }

        foundItems = new ArrayList<>();
        foundItems.add(currentInstance);
        lastMatchWasNothing = false;
        lastMatchWasInstance = true;
        return this;
    }

    @Override
    public boolean wasQueryIntendedToMatchAnInstance() {
        return wasIntentToMatchInstance;
    }

    @Override
    public boolean isResultACollection() {
        return isCollection;
    }

    @Override
    public List<EntityInstance> getListEntityInstances() {
        return new ArrayList<>(foundItems);
    }

    @Override
    public EntityInstance getLastInstance() {
        return currentInstance;
    }

    @Override
    public boolean lastMatchWasInstance() {
        return lastMatchWasInstance;
    }

    @Override
    public boolean lastMatchWasNothing() {
        return lastMatchWasNothing;
    }

    @Override
    public EntityDefinition resultContainsDefn() {
        return resultContainsDefinition;
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

    private static String[] termsFrom(final String query) {
        String normalized = query == null ? "" : query.trim();
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

    private EntityDefinition relatedEntityFor(
            final EntityInstance instance, final String relationshipName) {
        if (instance.getEntity().related().getRelationships(relationshipName).isEmpty()) {
            return null;
        }
        return instance.getEntity().related().getRelationships(relationshipName).get(0).getTo();
    }
}
