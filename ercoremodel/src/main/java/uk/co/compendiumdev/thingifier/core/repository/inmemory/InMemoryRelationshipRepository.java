package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;

final class InMemoryRelationshipRepository implements RelationshipRepository {

    private final InMemoryThingStore store;

    InMemoryRelationshipRepository(final InMemoryThingStore store) {
        this.store = store;
    }

    @Override
    public void connect(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        store.connectRelationship(from, relationshipName, to);
    }

    @Override
    public List<EntityInstance> listRelated(
            final EntityInstance instance,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        return store.listRelatedInstances(instance, relationshipName, queryParams);
    }

    @Override
    public void removeBetween(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        store.removeRelationshipsInvolving(parent, child, relationshipName);
    }

    @Override
    public void removeAll(final EntityInstance instance) {
        store.removeAllRelationships(instance);
    }

    @Override
    public boolean hasRelationships(final EntityInstance instance) {
        return store.hasRelationshipInstances(instance);
    }

    @Override
    public ValidationReport validate(final EntityInstance instance) {
        return store.validateRelationships(instance);
    }
}
