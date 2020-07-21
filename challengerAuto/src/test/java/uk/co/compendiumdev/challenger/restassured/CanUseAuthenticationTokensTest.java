package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.HashMap;

public class CanUseAuthenticationTokensTest extends RestAssuredBaseTest {

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

    @Test
    public void canNotAmendSecretNoteWithIncorrectToken(){

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                contentType(ContentType.JSON).
                body("{\"note\":\"my note\"}").
                header("X-AUTH-TOKEN","incorrecttoken").
            when().
                post(apiPath("/secret/note")).
            then().
                statusCode(403);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/note (403)").status);

    }

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

//    }
//
//    @Test
//    public void canGetSecretNoteWithAValidToken(){


        final HashMap note = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept(ContentType.JSON).
                    header("X-AUTH-TOKEN", validToken).
                when().
                    get(apiPath("/secret/note")).
                then().
                    statusCode(200).
                    extract().body().as(HashMap.class);

        statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /secret/note (200)").status);

        Assertions.assertEquals("my note", note.get("note"));

    }
}



