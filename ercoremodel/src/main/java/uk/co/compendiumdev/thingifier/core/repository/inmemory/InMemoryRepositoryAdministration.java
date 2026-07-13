package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.repository.RepositoryAdministration;

final class InMemoryRepositoryAdministration implements RepositoryAdministration {

    private final InMemoryThingStore store;

    InMemoryRepositoryAdministration(final InMemoryThingStore store) {
        this.store = store;
    }

    @Override
    public void initializeFrom(final ERSchema schema) {
        store.initializeFrom(schema);
    }

    @Override
    public void refreshSchema(final ERSchema schema) {
        store.refreshSchema(schema);
    }

    @Override
    public void clearAllData() {
        store.clearAllData();
    }

    @Override
    public void clearEntityData(final String entityName) {
        store.clearInstanceDataFor(entityName);
    }

    @Override
    public void resetAutoIncrementCounter(final EntityDefinition entity, final String fieldName) {
        store.resetAutoIncrementCounter(entity, fieldName);
    }

    @Override
    public boolean resetAutoIncrementCounterWhenNextValueAbove(
            final EntityDefinition entity, final String fieldName, final int ceiling) {
        return store.resetAutoIncrementCounterWhenNextValueAbove(entity, fieldName, ceiling);
    }

    @Override
    public void accommodateProtectedIds(
            final EntityDefinition entity, final List<NamedValue> fieldValues) {
        store.setNextIdCountersToAccomodate(entity, fieldValues);
    }
}
