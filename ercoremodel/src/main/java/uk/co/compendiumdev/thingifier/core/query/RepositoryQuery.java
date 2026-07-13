package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class RepositoryQuery implements RepositoryQueryResult {

    private final EntityInstanceQuery entityQuery;
    private final RelationshipRepository relationshipRepository;
    private final RepositoryQuerySpec spec;

    private boolean isCollection;
    private boolean wasIntentToMatchInstance;
    private boolean lastMatchWasInstance;
    private boolean lastMatchWasNothing = true;
    private EntityDefinition resultContainsDefinition;
    private EntityInstance currentInstance;
    private List<EntityInstance> foundItems = new ArrayList<>();

    public RepositoryQuery(final ThingStore store, final RepositoryQuerySpec spec) {
        this(store.entityQueries(), store.relationships(), spec);
    }

    public RepositoryQuery(
            final EntityInstanceQuery entityQuery,
            final RelationshipRepository relationshipRepository,
            final RepositoryQuerySpec spec) {
        this.entityQuery = entityQuery;
        this.relationshipRepository = relationshipRepository;
        this.spec = spec;
    }

    public RepositoryQuery performQuery() {
        return performQuery(new QueryFilterParams());
    }

    public RepositoryQuery performQuery(final QueryFilterParams queryParams) {
        resultContainsDefinition = spec.entity();

        if (!spec.hasIdentifier()) {
            isCollection = true;
            foundItems = new ArrayList<>(entityQuery.list(spec.entity(), queryParams));
            lastMatchWasNothing = false;
            return this;
        }

        wasIntentToMatchInstance = true;
        currentInstance = entityQuery.findByQueryIdentifier(spec.entity(), spec.identifier());
        if (currentInstance == null) {
            foundItems = new ArrayList<>();
            lastMatchWasNothing = true;
            lastMatchWasInstance = false;
            return this;
        }

        if (spec.hasRelationship()) {
            wasIntentToMatchInstance = true;
            isCollection = true;
            foundItems =
                    new ArrayList<>(
                            relationshipRepository.listRelated(
                                    currentInstance, spec.relationshipName(), queryParams));
            resultContainsDefinition = relatedEntityFor(currentInstance, spec.relationshipName());
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

    private EntityDefinition relatedEntityFor(
            final EntityInstance instance, final String relationshipName) {
        if (instance.getEntity().related().getRelationships(relationshipName).isEmpty()) {
            return null;
        }
        return instance.getEntity().related().getRelationships(relationshipName).get(0).getTo();
    }
}
