package uk.co.compendiumdev.challenger.restassured._08_put_update_challenges;

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
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class C022PutTodosWrongId400Test extends RestAssuredBaseTest {


    @Test
    void canFailToAmendATodoDueToAttemptToChangeId(){

        Todo amendMe = new TodosApi().getOrCreateAnyExistingTodo();

        int oldId = amendMe.id;
        int newId = oldId +1;
        amendMe.id= newId;
        // amendMe.id = - cannot amend the id as it is auto assigned
        // title is mandatory and must be in the message

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(amendMe).
                put(apiPath("/todos/" + oldId)).
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                extract().response();

        ErrorMessages error = response.body().as(ErrorMessages.class);
        Assertions.assertTrue(error.errorMessages.get(0).contains(String.format("Can not amend id from %d to %d", oldId, newId)));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /todos/{id} no amend id (400)").status);
    }

}
