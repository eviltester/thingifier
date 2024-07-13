package uk.co.compendiumdev.challenger.restassured._16_http_method_override_challenges;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C046PostOverrideToPatch500Test extends RestAssuredBaseTest {


    @Test
    void useOverrideHeaderToNotPatchHeartbeat500(){

        RestAssured.
                given().
                header("X-HTTP-Method-Override", "PATCH").
                header("X-CHALLENGER", xChallenger).
                request(
                        Method.POST,
                        apiPath("/heartbeat")).
                then().
                statusCode(500);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /heartbeat as PATCH (500)").status);

    }

}
