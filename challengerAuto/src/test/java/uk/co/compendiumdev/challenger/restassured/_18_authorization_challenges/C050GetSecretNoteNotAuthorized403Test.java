package uk.co.compendiumdev.challenger.restassured._18_authorization_challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C050GetSecretNoteNotAuthorized403Test extends RestAssuredBaseTest {

    @Test
    public void canNotGetSecretNoteWithIncorrectToken(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                header("X-AUTH-TOKEN","incorrecttoken").
                when().
                get(apiPath("/secret/note")).
                then().
                statusCode(403);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /secret/note (403)").status);

    }
}
