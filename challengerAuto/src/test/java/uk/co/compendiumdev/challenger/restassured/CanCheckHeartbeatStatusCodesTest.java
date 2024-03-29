package uk.co.compendiumdev.challenger.restassured;


import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.Map;

public class CanCheckHeartbeatStatusCodesTest extends RestAssuredBaseTest {


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


    // repeat above tests with the X-HTTP-Method-Override header and POST
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
