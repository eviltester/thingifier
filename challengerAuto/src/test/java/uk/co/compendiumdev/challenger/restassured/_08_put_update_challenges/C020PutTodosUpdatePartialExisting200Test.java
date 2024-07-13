package uk.co.compendiumdev.challenger.restassured._08_put_update_challenges;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class C020PutTodosUpdatePartialExisting200Test extends RestAssuredBaseTest {


    @Test
    void canAmendATodoWithPutUsingDefaults(){

        Todo amendMe = new TodosApi().getOrCreateAnyExistingTodo();

        // amendMe.id = - cannot amend the id as it is auto assigned
        amendMe.title = "my name " + System.currentTimeMillis(); // title is mandatory and must be in the message

        // if description not present default "" will be set
        // if doneStatus not present then default false will be set

        final JsonElement amendTodoJson = new Gson().toJsonTree(amendMe);
        amendTodoJson.getAsJsonObject().
                remove("doneStatus");
        amendTodoJson.getAsJsonObject().
                remove("description");



        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(amendTodoJson.toString()).
                put(apiPath("/todos/" + amendMe.id)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();

        Todo amendedTodo = response.body().as(Todo.class);

        Assertions.assertEquals(amendMe.id, amendedTodo.id);
        Assertions.assertEquals(amendMe.title, amendedTodo.title);
        Assertions.assertEquals("", amendedTodo.description);
        Assertions.assertEquals(false, amendedTodo.doneStatus);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /todos/{id} partial (200)").status);
    }

}
