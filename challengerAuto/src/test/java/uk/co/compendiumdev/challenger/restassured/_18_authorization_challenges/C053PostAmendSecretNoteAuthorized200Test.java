package uk.co.compendiumdev.challenger.restassured._18_authorization_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.HashMap;

public class C053PostAmendSecretNoteAuthorized200Test extends RestAssuredBaseTest {

    String validToken = "";

    private String getAuthToken(){
        if(validToken!=""){
            return validToken;
        }
        validToken =  RestAssured.
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

        return validToken;
    }

    @Test
    public void canAmendAndGetSecretNoteWithAValidToken(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                contentType(ContentType.JSON).
                body("{\"note\":\"my note\"}").
                header("X-AUTH-TOKEN", getAuthToken()).
                when().
                post(apiPath("/secret/note")).
                then().
                statusCode(200);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/note (200)").status);

    }
}
