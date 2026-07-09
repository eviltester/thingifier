package uk.co.compendiumdev.thingifier.core.repository;

import java.util.Set;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;

public interface ThingRepositoryProvider extends AutoCloseable {

    ThingRepository getDefaultRepository();

    ThingRepository getRepository(String databaseKey);

    Set<String> getRepositoryNames();

    ThingRepository createRepository(String databaseKey, ERSchema schema);

    boolean createRepositoryIfNotExisting(String databaseKey, ERSchema schema);

    void deleteRepository(String databaseKey);

    @Override
    default void close() {
        for (String databaseKey : getRepositoryNames()) {
            getRepository(databaseKey).close();
        }
    }
}
