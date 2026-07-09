package uk.co.compendiumdev.challenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.repository.InMemoryThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepositoryProvider;

import java.nio.file.Path;

public class ChallengerConfigRepositoryTest {

    @Test
    void defaultSimulationRepositoryIsMemory() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(new String[]{});

        Assertions.assertEquals(
                "memory",
                config.getSimulationRepositoryConfig().getRepositoryMode());

        try (ThingRepositoryProvider provider =
                     config.getSimulationRepositoryConfig().createProvider()) {
            Assertions.assertTrue(provider instanceof InMemoryThingRepositoryProvider);
        }
    }

    @Test
    void simRepositoryFlagCanUseSqliteMemory() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(
                new String[]{"-sim-repository=sqlite-memory"});

        Assertions.assertEquals(
                "sqlite-memory",
                config.getSimulationRepositoryConfig().getRepositoryMode());

        try (ThingRepositoryProvider provider =
                     config.getSimulationRepositoryConfig().createProvider()) {
            Assertions.assertTrue(provider instanceof SqliteThingRepositoryProvider);
        }
    }

    @Test
    void simSqliteMemoryAliasCanUseSqliteMemory() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(new String[]{"-sim-sqlite-memory"});

        Assertions.assertEquals(
                "sqlite-memory",
                config.getSimulationRepositoryConfig().getRepositoryMode());

        try (ThingRepositoryProvider provider =
                     config.getSimulationRepositoryConfig().createProvider()) {
            Assertions.assertTrue(provider instanceof SqliteThingRepositoryProvider);
        }
    }

    @Test
    void mainRepositorySqliteMemoryFlagDoesNotConfigureSimulationRepository() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(new String[]{"-sqlite-memory"});

        Assertions.assertEquals(
                "memory",
                config.getSimulationRepositoryConfig().getRepositoryMode());
    }

    @Test
    void simSqliteFileCanUseSeparateDirectory() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(new String[]{
                "-sim-repository=sqlite-file",
                "-sim-sqlite-directory=target/simulation-repository-test"
        });

        Assertions.assertEquals(
                "sqlite-file",
                config.getSimulationRepositoryConfig().getRepositoryMode());
        Assertions.assertEquals(
                Path.of("target/simulation-repository-test"),
                config.getSimulationRepositoryConfig().getSqliteDirectory());
    }
}
