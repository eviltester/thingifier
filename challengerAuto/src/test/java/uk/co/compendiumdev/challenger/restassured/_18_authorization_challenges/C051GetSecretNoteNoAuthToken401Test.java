package uk.co.compendiumdev.challenger.restassured._18_authorization_challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C051GetSecretNoteNoAuthToken401Test extends RestAssuredBaseTest {

    @Test
    public void canNotGetSecretNoteWithoutAToken(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                when().
                get(apiPath("/secret/note")).
                then().
                statusCode(401);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /secret/note (401)").status);

    }
}
