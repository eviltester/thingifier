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
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;

public class SqliteThingStoreProvider implements ThingStoreProvider {

    private final Map<String, ThingStore> repositories;
    private final Function<String, String> jdbcUrlFactory;

    private SqliteThingStoreProvider(final Function<String, String> jdbcUrlFactory) {
        this.repositories = new HashMap<>();
        this.jdbcUrlFactory = jdbcUrlFactory;
        repositories.put(
                EntityRelModel.DEFAULT_DATABASE_NAME,
                new SqliteThingStore(
                        EntityRelModel.DEFAULT_DATABASE_NAME,
                        jdbcUrlFactory.apply(EntityRelModel.DEFAULT_DATABASE_NAME)));
    }

    public static SqliteThingStoreProvider inMemory() {
        String providerName = "thingifier_" + UUID.randomUUID().toString().replace("-", "");
        return new SqliteThingStoreProvider(
                databaseKey ->
                        "jdbc:sqlite:file:"
                                + providerName
                                + "_"
                                + safeName(databaseKey)
                                + "?mode=memory&cache=shared");
    }

    public static SqliteThingStoreProvider fileBacked(final Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create SQLite repository directory " + directory, e);
        }

        return new SqliteThingStoreProvider(
                databaseKey -> {
                    Path databasePath = directory.resolve(safeName(databaseKey) + ".sqlite");
                    return "jdbc:sqlite:"
                            + databasePath.toAbsolutePath().toString().replace("\\", "/");
                });
    }

    public static SqliteThingStoreProvider fileBackedFile(final Path databaseFile) {
        final Path normalized = databaseFile.toAbsolutePath().normalize();
        final Path parent = normalized.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not create SQLite repository directory " + parent, e);
            }
        }

        return new SqliteThingStoreProvider(
                databaseKey ->
                        "jdbc:sqlite:"
                                + databaseFileFor(normalized, databaseKey)
                                        .toAbsolutePath()
                                        .toString()
                                        .replace("\\", "/"));
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

        ThingStore store = new SqliteThingStore(databaseKey, jdbcUrlFactory.apply(databaseKey));
        store.administration().initializeFrom(schema);
        repositories.put(databaseKey, store);
        return store;
    }

    @Override
    public boolean createStoreIfNotExisting(final String databaseKey, final ERSchema schema) {
        if (repositories.containsKey(databaseKey)) {
            return false;
        }

        ThingStore store = new SqliteThingStore(databaseKey, jdbcUrlFactory.apply(databaseKey));
        store.administration().initializeFrom(schema);
        repositories.put(databaseKey, store);
        return true;
    }

    @Override
    public void deleteStore(final String databaseKey) {
        ThingStore store = repositories.remove(databaseKey);
        if (store != null) {
            store.close();
        }
    }

    private static String safeName(final String databaseKey) {
        return databaseKey.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static Path databaseFileFor(final Path databaseFile, final String databaseKey) {
        if (EntityRelModel.DEFAULT_DATABASE_NAME.equals(databaseKey)) {
            return databaseFile;
        }

        final Path parent = databaseFile.getParent();
        final String fileName = databaseFile.getFileName().toString();
        final int extensionAt = fileName.lastIndexOf(".");
        final String baseName = extensionAt > 0 ? fileName.substring(0, extensionAt) : fileName;
        final String extension = extensionAt > 0 ? fileName.substring(extensionAt) : ".sqlite";
        final String sessionFileName = baseName + "-" + safeName(databaseKey) + extension;
        return parent == null ? Path.of(sessionFileName) : parent.resolve(sessionFileName);
    }
}
