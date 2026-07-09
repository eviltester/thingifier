package uk.co.compendiumdev.thingifier.core.repository.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepositoryProvider;

public class SqliteThingRepositoryProvider implements ThingRepositoryProvider {

    private final Map<String, ThingRepository> repositories;
    private final Function<String, String> jdbcUrlFactory;

    private SqliteThingRepositoryProvider(final Function<String, String> jdbcUrlFactory) {
        this.repositories = new HashMap<>();
        this.jdbcUrlFactory = jdbcUrlFactory;
        repositories.put(
                EntityRelModel.DEFAULT_DATABASE_NAME,
                new SqliteThingRepository(
                        EntityRelModel.DEFAULT_DATABASE_NAME,
                        jdbcUrlFactory.apply(EntityRelModel.DEFAULT_DATABASE_NAME)));
    }

    public static SqliteThingRepositoryProvider inMemory() {
        String providerName = "thingifier_" + UUID.randomUUID().toString().replace("-", "");
        return new SqliteThingRepositoryProvider(
                databaseKey ->
                        "jdbc:sqlite:file:"
                                + providerName
                                + "_"
                                + safeName(databaseKey)
                                + "?mode=memory&cache=shared");
    }

    public static SqliteThingRepositoryProvider fileBacked(final Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create SQLite repository directory " + directory, e);
        }

        return new SqliteThingRepositoryProvider(
                databaseKey -> {
                    Path databasePath = directory.resolve(safeName(databaseKey) + ".sqlite");
                    return "jdbc:sqlite:"
                            + databasePath.toAbsolutePath().toString().replace("\\", "/");
                });
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

        ThingRepository repository =
                new SqliteThingRepository(databaseKey, jdbcUrlFactory.apply(databaseKey));
        repository.initializeFrom(schema);
        repositories.put(databaseKey, repository);
        return repository;
    }

    @Override
    public boolean createRepositoryIfNotExisting(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            return false;
        }

        ThingRepository repository =
                new SqliteThingRepository(databaseKey, jdbcUrlFactory.apply(databaseKey));
        repository.initializeFrom(schema);
        repositories.put(databaseKey, repository);
        return true;
    }

    @Override
    public void deleteRepository(final String databaseKey) {
        ThingRepository repository = repositories.remove(databaseKey);
        if (repository != null) {
            repository.close();
        }
    }

    private static String safeName(final String databaseKey) {
        return databaseKey.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
