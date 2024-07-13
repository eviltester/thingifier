package uk.co.compendiumdev.challenger.restassured._06_put_creation_challenges;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C016PutTodosFailCreate400Test extends RestAssuredBaseTest {

    @Test
    void canFailToCreateTodoWithPut(){

        Todo createMe = new Todo();
        createMe.id = 200;
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                put(apiPath("/todos/" + createMe.id)).
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                extract().response();

        ErrorMessages errors = response.body().as(ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.size());
        Assertions.assertEquals("Cannot create todo with PUT due to Auto fields id", errors.errorMessages.get(0));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /todos/{id} (400)").status);
    }

}