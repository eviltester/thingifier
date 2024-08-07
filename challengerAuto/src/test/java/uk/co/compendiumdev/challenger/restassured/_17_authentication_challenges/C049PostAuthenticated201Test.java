package uk.co.compendiumdev.challenger.restassured._17_authentication_challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C049PostAuthenticated201Test extends RestAssuredBaseTest {

    @Test
    public void canGetASecretTokenWithCorrectAuth(){

        String token = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                    contentType("application/json").
                auth().preemptive().basic("admin","password").
                when().
                    post(apiPath("/secret/token")).
                then().
                    statusCode(201).
                    extract().
                    header("X-AUTH-TOKEN");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/token (201)").status);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.length()>10);
    }
}
