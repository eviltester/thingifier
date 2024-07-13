package uk.co.compendiumdev.challenger.restassured._15_status_code_challenges;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C043TraceReceiveStatusCode501Test extends RestAssuredBaseTest {

    @Test
    void canNotTraceHeartbeat501(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                request(
                        Method.TRACE,
                        apiPath("/heartbeat")).
                then().
                statusCode(501);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("TRACE /heartbeat (501)").status);

    }

}
