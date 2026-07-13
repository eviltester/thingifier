package uk.co.compendiumdev.sparkstart;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * IDE convenience runners. This class name deliberately avoids Surefire's default *Test naming
 * pattern so each profile can be right-clicked manually.
 */
public class ChallengerAutoRunProfiles {

    @Test
    void localMemorySinglePlayer() {
        runProfile(
                ChallengerAutoConfig.localProfile(
                        ChallengerAutoConfig.Repository.MEMORY,
                        ChallengerAutoConfig.PlayerMode.SINGLE));
    }

    @Test
    void localMemoryMultiPlayer() {
        runProfile(
                ChallengerAutoConfig.localProfile(
                        ChallengerAutoConfig.Repository.MEMORY,
                        ChallengerAutoConfig.PlayerMode.MULTI));
    }

    @Test
    void localSqliteMemorySinglePlayer() {
        runProfile(
                ChallengerAutoConfig.localProfile(
                        ChallengerAutoConfig.Repository.SQLITE_MEMORY,
                        ChallengerAutoConfig.PlayerMode.SINGLE));
    }

    @Test
    void localSqliteMemoryMultiPlayer() {
        runProfile(
                ChallengerAutoConfig.localProfile(
                        ChallengerAutoConfig.Repository.SQLITE_MEMORY,
                        ChallengerAutoConfig.PlayerMode.MULTI));
    }

    @Test
    void existingLocal() {
        runProfile(ChallengerAutoConfig.existingLocal());
    }

    @Test
    void liveSmoke() {
        runProfile(ChallengerAutoConfig.liveSmoke());
    }

    private void runProfile(final ChallengerAutoConfig config) {
        Environment.useConfiguration(config);
        assertSmokeEndpoints(Environment.getBaseUri());
    }

    private void assertSmokeEndpoints(final String baseUrl) {
        RestAssured.given().get(baseUrl + "/heartbeat").then().statusCode(204);

        RestAssured.given()
                .accept(ContentType.JSON)
                .get(baseUrl + "/challenges")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
