package uk.co.compendiumdev.serverstart;

import io.restassured.RestAssured;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class ChallengerAutoRuntimeHarnessCheck {

    @Test
    void localMemorySinglePlayerCanStartInOwnedJvm() {
        assumeCurrentRunAllowsOwnedLocalHarnesses();

        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "local",
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "memory",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "single",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PORT, "auto"),
                        Collections.emptyMap());

        assertOwnedLocalRuntimeStarts(config);
    }

    @Test
    void localSqliteMemoryMultiplayerCanStartInOwnedJvm() {
        assumeCurrentRunAllowsOwnedLocalHarnesses();

        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "local",
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "sqlite-memory",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "multi",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PORT, "auto"),
                        Collections.emptyMap());

        assertOwnedLocalRuntimeStarts(config);
    }

    @Test
    void existingTargetDoesNotRepresentOwnedLocalProcess() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_TARGET, "existing"),
                        Collections.emptyMap());

        Assertions.assertFalse(config.startsOwnedLocalProcess());
    }

    @Test
    void externalTargetsAreSmokeOnlyUnlessOptedIn() {
        ChallengerAutoConfig smokeOnly =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_TARGET, "live"),
                        Collections.emptyMap());
        ChallengerAutoConfig full =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "live",
                                ChallengerAutoConfig.PROPERTY_EXTERNAL_FULL, "true"),
                        Collections.emptyMap());

        Assertions.assertFalse(smokeOnly.shouldRunFullSuite());
        Assertions.assertTrue(full.shouldRunFullSuite());
    }

    private void assertOwnedLocalRuntimeStarts(final ChallengerAutoConfig config) {
        try (ChallengerAutoRuntime runtime = ChallengerAutoRuntime.start(config)) {
            Assertions.assertTrue(runtime.ownsLocalProcess());
            RestAssured.given().get(runtime.getBaseUrl() + "/heartbeat").then().statusCode(204);
        }
    }

    private void assumeCurrentRunAllowsOwnedLocalHarnesses() {
        Assumptions.assumeTrue(
                ChallengerAutoConfig.current().getTarget() == ChallengerAutoConfig.Target.LOCAL,
                "Owned local harness tests run only for challenger.auto.target=local");
    }
}
