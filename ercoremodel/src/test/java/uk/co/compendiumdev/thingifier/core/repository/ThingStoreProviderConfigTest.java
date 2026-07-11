package uk.co.compendiumdev.thingifier.core.repository;

import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class ThingStoreProviderConfigTest {

    @TempDir Path tempDir;

    @Test
    public void defaultsToCurrentInMemoryRepository() {
        ThingStoreProviderConfig config = ThingStoreProviderConfig.fromArgs(new String[] {});

        Assertions.assertEquals("memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof InMemoryThingStoreProvider);
    }

    @Test
    public void canCreateSqliteInMemoryRepositoryFromArgs() {
        ThingStoreProviderConfig config =
                ThingStoreProviderConfig.fromArgs(
                        new String[] {"-thingifier-repository=sqlite-memory"});

        Assertions.assertEquals("sqlite-memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingStoreProvider);
    }

    @Test
    public void canCreateSqliteInMemoryRepositoryFromChallengeMainShortcutArg() {
        ThingStoreProviderConfig config =
                ThingStoreProviderConfig.fromArgs(new String[] {"-sqlite-memory"});

        Assertions.assertEquals("sqlite-memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingStoreProvider);
    }

    @Test
    public void canCreateSqliteFileRepositoryFromArgs() {
        ThingStoreProviderConfig config =
                ThingStoreProviderConfig.fromArgs(
                        new String[] {
                            "-thingifier-repository=sqlite-file",
                            "-thingifier-sqlite-directory=" + tempDir
                        });

        Assertions.assertEquals("sqlite-file", config.getRepositoryMode());
        Assertions.assertEquals(tempDir, config.getSqliteDirectory());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingStoreProvider);
    }

    @Test
    public void rejectsUnknownRepositoryModes() {
        ThingStoreProviderConfig config =
                ThingStoreProviderConfig.fromArgs(new String[] {"-thingifier-repository=unknown"});

        IllegalArgumentException thrown =
                Assertions.assertThrows(IllegalArgumentException.class, config::createProvider);

        Assertions.assertTrue(thrown.getMessage().contains("Unknown Thingifier repository mode"));
    }
}
