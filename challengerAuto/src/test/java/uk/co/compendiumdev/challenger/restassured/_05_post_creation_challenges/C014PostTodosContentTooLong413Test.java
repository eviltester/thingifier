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

public class C014PostTodosContentTooLong413Test extends RestAssuredBaseTest {

    @Test
    void can400NotCreateATodoBecausePayloadIsTooLarge(){

        Todo createMe = new Todo();
        createMe.title = "my title";
        createMe.description = "my description " + System.currentTimeMillis();

        // add an extra field which makes payload too large
        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("blowoutpayload", stringOfLength(5000));

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(413).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (413) content too long").status);

    }

    private String stringOfLength(int length) {
        return "a".repeat(Math.max(0, length));
    }


}