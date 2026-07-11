package uk.co.compendiumdev.challenge;

import java.nio.file.Path;
import java.nio.file.Paths;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProviderConfig;

public class ChallengerConfig {

    public static final String ARG_SIM_REPOSITORY = "-sim-repository";
    public static final String ARG_SIM_SQLITE_MEMORY = "-sim-sqlite-memory";
    public static final String ARG_SIM_SQLITE_DIRECTORY = "-sim-sqlite-directory";
    public static final String DEFAULT_SIM_SQLITE_DIRECTORY = "thingifier-sim-sqlite";

    public boolean single_player_mode = true;
    public boolean isAdminApiEnabled = false;
    public boolean guiStayAlive = false;
    public PersistenceLayer persistenceLayer =
            new PersistenceLayer(PersistenceLayer.StorageType.LOCAL);
    private ThingStoreProviderConfig simulationRepositoryConfig =
            defaultSimulationRepositoryConfig();

    public static ThingStoreProviderConfig defaultSimulationRepositoryConfig() {
        return new ThingStoreProviderConfig(
                ThingStoreProviderConfig.DEFAULT_REPOSITORY_MODE,
                Paths.get(DEFAULT_SIM_SQLITE_DIRECTORY));
    }

    public void setSimulationRepositoryFromArgs(final String[] args) {
        String repositoryMode = ThingStoreProviderConfig.DEFAULT_REPOSITORY_MODE;
        if (hasArg(args, ARG_SIM_SQLITE_MEMORY)) {
            repositoryMode = "sqlite-memory";
        } else {
            String configuredMode = argValue(args, ARG_SIM_REPOSITORY);
            if (configuredMode != null && !configuredMode.trim().isEmpty()) {
                repositoryMode = configuredMode;
            }
        }

        String sqliteDirectory = argValue(args, ARG_SIM_SQLITE_DIRECTORY);
        if (sqliteDirectory == null || sqliteDirectory.trim().isEmpty()) {
            sqliteDirectory = DEFAULT_SIM_SQLITE_DIRECTORY;
        }

        simulationRepositoryConfig =
                new ThingStoreProviderConfig(repositoryMode, Path.of(sqliteDirectory));
    }

    public ThingStoreProviderConfig getSimulationRepositoryConfig() {
        return simulationRepositoryConfig;
    }

    public void setToMultiPlayerMode() {
        single_player_mode = false;
    }

    public void setToCloudPersistenceMode() {
        persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.CLOUD);
    }

    public void setGuiToKeepSessionAlive() {
        guiStayAlive = true;
    }

    public void setToNoPersistenceMode() {
        persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.NONE);
    }

    public void enableAdminApi() {
        isAdminApiEnabled = true;
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
}
