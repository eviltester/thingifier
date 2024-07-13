package uk.co.compendiumdev.challenger.restassured._15_status_code_challenges;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C044GetReceiveStatusCode204Test extends RestAssuredBaseTest {


    @Test
    void canGetHeartbeat(){

        // heartbeat returns 204 when running
        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                get(apiPath( "/heartbeat")).
                then().
                statusCode(204);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /heartbeat (204)").status);

    }

}
