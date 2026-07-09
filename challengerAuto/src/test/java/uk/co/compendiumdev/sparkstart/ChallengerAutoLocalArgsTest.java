package uk.co.compendiumdev.sparkstart;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengerAutoLocalArgsTest {

    @Test
    void memorySinglePlayerArgsUseMemoryRepositoryAndNoMultiplayerFlag() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "memory",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "single"),
                        Collections.emptyMap());

        Assertions.assertEquals(
                List.of("-port=4567", "-thingifier-repository=memory"),
                config.challengeMainArgs(4567));
    }

    @Test
    void sqliteMemoryMultiplayerArgsUseExistingShortcutFlag() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "sqlite-memory",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "multi"),
                        Collections.emptyMap());

        Assertions.assertEquals(
                List.of("-port=4568", "-sqlite-memory", "-multiplayer"),
                config.challengeMainArgs(4568));
    }

    @Test
    void sqliteFileArgsUseRepositoryModeFlag() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "sqlite-file",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "single"),
                        Collections.emptyMap());

        Assertions.assertEquals(
                List.of("-port=4569", "-thingifier-repository=sqlite-file"),
                config.challengeMainArgs(4569));
    }

    @Test
    void extraArgsAreAppendedInOrder() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_LOCAL_EXTRA_ARGS,
                                "-unlimitedtodos,-enableadminapi"),
                        Collections.emptyMap());

        Assertions.assertEquals(
                List.of(
                        "-port=4570",
                        "-sqlite-memory",
                        "-multiplayer",
                        "-unlimitedtodos",
                        "-enableadminapi"),
                config.challengeMainArgs(4570));
    }
}
