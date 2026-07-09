package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepositoryProvider;

public class InMemoryThingRepositoryProvider implements ThingRepositoryProvider {

    private final Map<String, ThingRepository> repositories;

    public InMemoryThingRepositoryProvider() {
        this(new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    public InMemoryThingRepositoryProvider(final ThingRepository defaultRepository) {
        repositories = new HashMap<>();
        repositories.put(EntityRelModel.DEFAULT_DATABASE_NAME, defaultRepository);
    }

    @Override
    public ThingRepository getDefaultRepository() {
        return repositories.get(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Override
    public ThingRepository getRepository(final String databaseKey) {
        return repositories.get(databaseKey);
    }

    @Override
    public Set<String> getRepositoryNames() {
        return new HashSet<>(repositories.keySet());
    }

    @Override
    public ThingRepository createRepository(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            throw new IllegalStateException("ERM Database Already Exists with name " + databaseKey);
        }

        ThingRepository repository = new InMemoryThingRepository(databaseKey);
        repository.initializeFrom(schema);
        repositories.put(databaseKey, repository);
        return repository;
    }

    @Override
    public boolean createRepositoryIfNotExisting(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            return false;
        }

        ThingRepository repository = new InMemoryThingRepository(databaseKey);
        repository.initializeFrom(schema);
        repositories.put(databaseKey, repository);
        return true;
    }

    @Override
    public void deleteRepository(final String databaseKey) {
        repositories.remove(databaseKey);
    }
}
