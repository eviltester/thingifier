package uk.co.compendiumdev.challenger.restassured._16_http_method_override_challenges;


import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C047PostOverrideToTrace501Test extends RestAssuredBaseTest {

    @Test
    void useOverrideHeaderToNotTraceHeartbeat501(){

        RestAssured.
                given().
                header("X-HTTP-Method-Override", "TRACE").
                header("X-CHALLENGER", xChallenger).
                request(
                        Method.POST,
                        apiPath("/heartbeat")).
                then().
                statusCode(501);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /heartbeat as Trace (501)").status);

    }

}
