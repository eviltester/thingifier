package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepositoryProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ThingRepositoryProviderConfig {

    public static final String DEFAULT_REPOSITORY_MODE = "memory";
    public static final String ARG_REPOSITORY_MODE = "-thingifier-repository";
    public static final String ARG_SQLITE_DIRECTORY = "-thingifier-sqlite-directory";
    public static final String ARG_SQLITE_MEMORY = "-sqlite-memory";
    public static final String ENV_REPOSITORY_MODE = "THINGIFIER_REPOSITORY";
    public static final String ENV_SQLITE_DIRECTORY = "THINGIFIER_SQLITE_DIRECTORY";
    public static final String PROPERTY_REPOSITORY_MODE = "thingifier.repository";
    public static final String PROPERTY_SQLITE_DIRECTORY = "thingifier.sqlite.directory";

    private final String repositoryMode;
    private final Path sqliteDirectory;

    public ThingRepositoryProviderConfig(final String repositoryMode, final Path sqliteDirectory) {
        this.repositoryMode = normalize(repositoryMode);
        this.sqliteDirectory = sqliteDirectory;
    }

    public static ThingRepositoryProviderConfig fromArgs(final String[] args) {
        String repositoryMode;
        if (hasArg(args, ARG_SQLITE_MEMORY)) {
            repositoryMode = "sqlite-memory";
        } else {
            repositoryMode = firstNonBlank(
                    argValue(args, ARG_REPOSITORY_MODE),
                    System.getProperty(PROPERTY_REPOSITORY_MODE),
                    System.getenv(ENV_REPOSITORY_MODE),
                    DEFAULT_REPOSITORY_MODE);
        }

        String sqliteDirectory = firstNonBlank(
                argValue(args, ARG_SQLITE_DIRECTORY),
                System.getProperty(PROPERTY_SQLITE_DIRECTORY),
                System.getenv(ENV_SQLITE_DIRECTORY),
                "thingifier-sqlite");

        return new ThingRepositoryProviderConfig(repositoryMode, Paths.get(sqliteDirectory));
    }

    public ThingRepositoryProvider createProvider() {
        switch (repositoryMode) {
            case "memory":
            case "in-memory":
            case "custom-memory":
                return new InMemoryThingRepositoryProvider();
            case "sqlite":
            case "sqlite-memory":
            case "sqlite-in-memory":
                return SqliteThingRepositoryProvider.inMemory();
            case "sqlite-file":
            case "sqlite-disk":
            case "file":
                return SqliteThingRepositoryProvider.fileBacked(sqliteDirectory);
            default:
                throw new IllegalArgumentException(
                        "Unknown Thingifier repository mode " + repositoryMode +
                                ". Expected memory, sqlite-memory, or sqlite-file.");
        }
    }

    public String getRepositoryMode() {
        return repositoryMode;
    }

    public Path getSqliteDirectory() {
        return sqliteDirectory;
    }

    public String describe() {
        if (repositoryMode.equals("sqlite-file") ||
                repositoryMode.equals("sqlite-disk") ||
                repositoryMode.equals("file")) {
            return repositoryMode + " at " + sqliteDirectory.toAbsolutePath();
        }
        return repositoryMode;
    }

    private static String argValue(final String[] args, final String argName) {
        if (args == null) {
            return null;
        }

        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.equalsIgnoreCase(argName)) {
                return "";
            }
            if (arg.toLowerCase().startsWith(argName.toLowerCase() + "=")) {
                return arg.substring(arg.indexOf("=") + 1).trim();
            }
        }
        return null;
    }

    private static boolean hasArg(final String[] args, final String argName) {
        if (args == null) {
            return false;
        }

        for (String arg : args) {
            if (arg != null && arg.equalsIgnoreCase(argName)) {
                return true;
            }
        }
        return false;
    }

    private static String firstNonBlank(final String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String normalize(final String mode) {
        return firstNonBlank(mode, DEFAULT_REPOSITORY_MODE).
                trim().
                toLowerCase().
                replace("_", "-");
    }
}
