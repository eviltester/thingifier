package uk.co.compendiumdev.thingifier.core.repository;

import java.util.Set;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;

public interface ThingStoreProvider extends AutoCloseable {

    ThingStore getDefaultStore();

    ThingStore getStore(String databaseKey);

    Set<String> getStoreNames();

    ThingStore createStore(String databaseKey, ERSchema schema);

    boolean createStoreIfNotExisting(String databaseKey, ERSchema schema);

    void deleteStore(String databaseKey);

    @Override
    default void close() {
        for (String databaseKey : getStoreNames()) {
            getStore(databaseKey).close();
        }
    }
}
