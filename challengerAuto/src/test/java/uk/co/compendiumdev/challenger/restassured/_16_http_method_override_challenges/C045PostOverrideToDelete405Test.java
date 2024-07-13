package uk.co.compendiumdev.challenger.restassured._16_http_method_override_challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C045PostOverrideToDelete405Test extends RestAssuredBaseTest {


    @Test
    void useOverrideHeaderToNotDeleteHeartbeat(){

        // heartbeat returns 204 when running
        RestAssured.
                given().
                header("X-HTTP-Method-Override", "DELETE").
                header("X-CHALLENGER", xChallenger).
                post(apiPath( "/heartbeat")).
                then().
                statusCode(405);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /heartbeat as DELETE (405)").status);

    }

}
