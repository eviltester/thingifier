package uk.co.compendiumdev.challenger.restassured._05_post_creation_challenges;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C010PostTodosFailValidationDoneStatus400Test extends RestAssuredBaseTest {

    @Test
    void can400NotCreateATodoWithInvalidDoneStatusPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();

        // cannot create an invalid status with an invalid boolean value so...
        // createMe.doneStatus = true;
        // work with the JSON to create a 'bad' payload

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("doneStatus", "truthy");

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(400).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) doneStatus").status);

    }

}