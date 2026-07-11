package uk.co.compendiumdev.thingifier.core.repository.sqlite;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;

final class SqliteRelationshipRepository implements RelationshipRepository {

    private final SqliteThingStore store;

    SqliteRelationshipRepository(final SqliteThingStore store) {
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
    public List<EntityInstance> removeBetween(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        return store.removeRelationshipsInvolving(parent, child, relationshipName);
    }

    @Override
    public List<EntityInstance> removeAll(final EntityInstance instance) {
        return store.removeAllRelationships(instance);
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
