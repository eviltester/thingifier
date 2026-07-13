package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.sparkstart.Environment;

public class ChallengerAutoSmokeTest {

    @Test
    void heartbeatEndpointIsAvailable() {
        RestAssured.given().get(Environment.getEnv("/heartbeat")).then().statusCode(204);
    }

    @Test
    void challengesEndpointIsAvailable() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .get(Environment.getEnv("/challenges"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
