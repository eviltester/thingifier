package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;

public class InMemoryThingStoreProvider implements ThingStoreProvider {

    private final Map<String, ThingStore> repositories;

    public InMemoryThingStoreProvider() {
        this(new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    public InMemoryThingStoreProvider(final ThingStore defaultStore) {
        repositories = new HashMap<>();
        repositories.put(EntityRelModel.DEFAULT_DATABASE_NAME, defaultStore);
    }

    @Override
    public ThingStore getDefaultStore() {
        return repositories.get(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Override
    public ThingStore getStore(final String databaseKey) {
        return repositories.get(databaseKey);
    }

    @Override
    public Set<String> getStoreNames() {
        return new HashSet<>(repositories.keySet());
    }

    @Override
    public ThingStore createStore(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            throw new IllegalStateException("ERM Database Already Exists with name " + databaseKey);
        }

        ThingStore store = new InMemoryThingStore(databaseKey);
        store.administration().initializeFrom(schema);
        repositories.put(databaseKey, store);
        return store;
    }

    @Override
    public boolean createStoreIfNotExisting(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            return false;
        }

        ThingStore store = new InMemoryThingStore(databaseKey);
        store.administration().initializeFrom(schema);
        repositories.put(databaseKey, store);
        return true;
    }

    @Override
    public void deleteStore(final String databaseKey) {
        repositories.remove(databaseKey);
    }
}
