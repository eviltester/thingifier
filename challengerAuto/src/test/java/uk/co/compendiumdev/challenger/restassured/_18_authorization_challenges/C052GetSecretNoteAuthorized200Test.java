package uk.co.compendiumdev.challenger.restassured._18_authorization_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.HashMap;

public class C052GetSecretNoteAuthorized200Test extends RestAssuredBaseTest {

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
                body("{\"note\":\"my amended note text\"}").
                header("X-AUTH-TOKEN", getAuthToken()).
                when().
                post(apiPath("/secret/note")).
                then().
                statusCode(200);

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

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /secret/note (200)").status);

        Assertions.assertEquals("my amended note text", note.get("note"));

    }
}
