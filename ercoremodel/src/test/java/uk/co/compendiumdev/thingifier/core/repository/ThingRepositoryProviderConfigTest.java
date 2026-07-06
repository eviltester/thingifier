package uk.co.compendiumdev.thingifier.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class ThingRepositoryProviderConfigTest {

    @TempDir
    Path tempDir;

    @Test
    public void defaultsToCurrentInMemoryRepository() {
        ThingRepositoryProviderConfig config = ThingRepositoryProviderConfig.fromArgs(new String[]{});

        Assertions.assertEquals("memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof InMemoryThingRepositoryProvider);
    }

    @Test
    public void canCreateSqliteInMemoryRepositoryFromArgs() {
        ThingRepositoryProviderConfig config = ThingRepositoryProviderConfig.fromArgs(
                new String[]{"-thingifier-repository=sqlite-memory"});

        Assertions.assertEquals("sqlite-memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingRepositoryProvider);
    }

    @Test
    public void canCreateSqliteInMemoryRepositoryFromChallengeMainShortcutArg() {
        ThingRepositoryProviderConfig config = ThingRepositoryProviderConfig.fromArgs(
                new String[]{"-sqlite-memory"});

        Assertions.assertEquals("sqlite-memory", config.getRepositoryMode());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingRepositoryProvider);
    }

    @Test
    public void canCreateSqliteFileRepositoryFromArgs() {
        ThingRepositoryProviderConfig config = ThingRepositoryProviderConfig.fromArgs(
                new String[]{
                        "-thingifier-repository=sqlite-file",
                        "-thingifier-sqlite-directory=" + tempDir
                });

        Assertions.assertEquals("sqlite-file", config.getRepositoryMode());
        Assertions.assertEquals(tempDir, config.getSqliteDirectory());
        Assertions.assertTrue(config.createProvider() instanceof SqliteThingRepositoryProvider);
    }

    @Test
    public void rejectsUnknownRepositoryModes() {
        ThingRepositoryProviderConfig config = ThingRepositoryProviderConfig.fromArgs(
                new String[]{"-thingifier-repository=unknown"});

        IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                config::createProvider);

        Assertions.assertTrue(thrown.getMessage().contains("Unknown Thingifier repository mode"));
    }
}
