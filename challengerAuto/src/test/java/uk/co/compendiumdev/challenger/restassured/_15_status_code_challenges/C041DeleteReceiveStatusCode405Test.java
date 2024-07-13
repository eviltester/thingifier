package uk.co.compendiumdev.challenger.restassured._15_status_code_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C041DeleteReceiveStatusCode405Test extends RestAssuredBaseTest {

    @Test
    void canNotDeleteHeartbeat(){

        // heartbeat returns 204 when running
        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                delete(apiPath( "/heartbeat")).
                then().
                statusCode(405);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("DELETE /heartbeat (405)").status);

    }

}
