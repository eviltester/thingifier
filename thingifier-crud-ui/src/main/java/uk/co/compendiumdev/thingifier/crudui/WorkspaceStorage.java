package uk.co.compendiumdev.thingifier.crudui;

import java.nio.file.Path;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProviderConfig;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public final class WorkspaceStorage {

    public static final String MODE_MEMORY = "memory";
    public static final String MODE_SQLITE_MEMORY = "sqlite-memory";
    public static final String MODE_SQLITE_FILE = "sqlite-file";

    private final String mode;
    private final Path sqliteFile;

    private WorkspaceStorage(final String mode, final Path sqliteFile) {
        this.mode = mode;
        this.sqliteFile = sqliteFile == null ? null : sqliteFile.toAbsolutePath().normalize();
    }

    public static WorkspaceStorage memory() {
        return new WorkspaceStorage(MODE_MEMORY, null);
    }

    public static WorkspaceStorage sqliteMemory() {
        return new WorkspaceStorage(MODE_SQLITE_MEMORY, null);
    }

    public static WorkspaceStorage sqliteFile(final Path sqliteFile) {
        if (sqliteFile == null || sqliteFile.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("SQLite file path is required");
        }
        return new WorkspaceStorage(MODE_SQLITE_FILE, sqliteFile);
    }

    public static WorkspaceStorage fromConfig(final ThingStoreProviderConfig config) {
        String mode = normalize(config.getRepositoryMode());
        if (MODE_SQLITE_MEMORY.equals(mode)
                || "sqlite".equals(mode)
                || "sqlite-in-memory".equals(mode)) {
            return sqliteMemory();
        }
        if (MODE_SQLITE_FILE.equals(mode) || "sqlite-disk".equals(mode) || "file".equals(mode)) {
            if (config.hasSqliteFile()) {
                return sqliteFile(config.getSqliteFile());
            }
            return sqliteFile(config.getSqliteDirectory().resolve("thingifier.sqlite"));
        }
        return memory();
    }

    public static WorkspaceStorage fromModeAndPath(final String mode, final String sqliteFile) {
        String normalizedMode = normalize(mode);
        if (normalizedMode.isEmpty() || MODE_MEMORY.equals(normalizedMode)) {
            return memory();
        }
        if (MODE_SQLITE_MEMORY.equals(normalizedMode)
                || "sqlite".equals(normalizedMode)
                || "sqlite-in-memory".equals(normalizedMode)) {
            return sqliteMemory();
        }
        if (MODE_SQLITE_FILE.equals(normalizedMode)
                || "sqlite-disk".equals(normalizedMode)
                || "file".equals(normalizedMode)) {
            return sqliteFile(Path.of(nullToEmpty(sqliteFile).trim()));
        }
        throw new IllegalArgumentException("Unknown workspace storage mode " + mode);
    }

    public ThingStoreProvider provider() {
        if (MODE_SQLITE_MEMORY.equals(mode)) {
            return SqliteThingStoreProvider.inMemory();
        }
        if (MODE_SQLITE_FILE.equals(mode)) {
            return SqliteThingStoreProvider.fileBackedFile(sqliteFile);
        }
        return new InMemoryThingStoreProvider();
    }

    public String mode() {
        return mode;
    }

    public boolean isSqliteFile() {
        return MODE_SQLITE_FILE.equals(mode);
    }

    public String sqliteFilePath() {
        return sqliteFile == null ? "" : sqliteFile.toString();
    }

    public Path sqliteFile() {
        return sqliteFile;
    }

    private static String normalize(final String value) {
        return nullToEmpty(value).trim().toLowerCase().replace("_", "-");
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
