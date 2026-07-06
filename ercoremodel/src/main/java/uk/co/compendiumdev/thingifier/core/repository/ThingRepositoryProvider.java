package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;

import java.util.Set;

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
