package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.HashMap;

public class CanUseBearerAuthenticationTokenTest extends RestAssuredBaseTest {

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
    public void canAmendAndGetSecretNoteWithBearerAuthenticationAndValidToken(){

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                contentType(ContentType.JSON).
                body("{\"note\":\"my note\"}").
                auth().oauth2(getAuthToken()).
            when().
                post(apiPath("/secret/note")).
            then().
                statusCode(200);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /secret/note (Bearer)").status);



        final HashMap note = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept(ContentType.JSON).
                    auth().oauth2(getAuthToken()).
                when().
                    get(apiPath("/secret/note")).
                then().
                    statusCode(200).
                    extract().body().as(HashMap.class);

        statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /secret/note (Bearer)").status);

        Assertions.assertEquals("my note", note.get("note"));

    }

    @Test
    public void canNotAmendWithInvalidBearerAuthenticationToken() {

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                contentType(ContentType.JSON).
                body("{\"note\":\"my note\"}").
                auth().oauth2(getAuthToken()+"wrong").
                when().
                post(apiPath("/secret/note")).
                then().
                statusCode(403);
    }

    @Test
    public void canNotGetSecretNoteWithInvalidBearerAuthenticationToken(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept(ContentType.JSON).
                auth().oauth2(getAuthToken()+"wrong").
                when().
                get(apiPath("/secret/note")).
                then().
                statusCode(403);

    }
}



