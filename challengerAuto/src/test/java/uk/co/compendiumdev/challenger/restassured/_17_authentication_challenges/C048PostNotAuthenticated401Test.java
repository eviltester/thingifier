package uk.co.compendiumdev.challenger.restassured._17_authentication_challenges;


import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C048PostNotAuthenticated401Test extends RestAssuredBaseTest {

    @Test
    public void canNotGetASecretTokenWithWrongAuth(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                auth().preemptive().basic("admin","incorrect").
                when().
                post(apiPath("/secret/token")).
                then().
                statusCode(401).
                header("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/token (401)").status);
    }

}
