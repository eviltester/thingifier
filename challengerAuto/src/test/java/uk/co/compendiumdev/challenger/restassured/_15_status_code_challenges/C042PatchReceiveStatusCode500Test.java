package uk.co.compendiumdev.challenger.restassured._15_status_code_challenges;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C042PatchReceiveStatusCode500Test extends RestAssuredBaseTest {

    @Test
    void canNotPatchHeartbeat500(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                request(
                        Method.PATCH,
                        apiPath("/heartbeat")).
                then().
                statusCode(500);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PATCH /heartbeat (500)").status);

    }

}
