package uk.co.compendiumdev.challenger.restassured._18_authorization_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C054PostNoAuthToken401Test extends RestAssuredBaseTest {

    @Test
    public void canNotAmendSecretNoteWithoutAToken(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                contentType(ContentType.JSON).
                body("{\"note\":\"my note\"}").
                when().
                post(apiPath("/secret/note")).
                then().
                statusCode(401);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/note (401)").status);

    }
}
