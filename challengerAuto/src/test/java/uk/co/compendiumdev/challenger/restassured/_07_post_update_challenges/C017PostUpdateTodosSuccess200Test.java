package uk.co.compendiumdev.challenger.restassured._07_post_update_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.List;

public class C017PostUpdateTodosSuccess200Test extends RestAssuredBaseTest {

    @Test
    void canUpdateATodoWithPost(){

        TodosApi api = new TodosApi();

        final Todo todo = api.createTodo("created", "created this", false);

        Todo updatedDetails = new Todo();
        updatedDetails.id = todo.id;
        updatedDetails.title = "Title Updated " + System.currentTimeMillis();
        updatedDetails.description = "Description Updated " + System.currentTimeMillis();
        updatedDetails.doneStatus=true;

        final Response response = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                    contentType("application/json").
                    body(updatedDetails).
                post(apiPath("/todos/" + todo.id)).
                then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                extract().response();

        Todo updatedTodo = response.body().as(Todo.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos/{id} (200)").status);

        // check it reported as updated in the details of the response
        Assertions.assertEquals(updatedDetails.id, updatedTodo.id);
        Assertions.assertEquals(updatedDetails.title, updatedTodo.title);
        Assertions.assertEquals(updatedDetails.description, updatedTodo.description);
        Assertions.assertEquals(updatedDetails.doneStatus, updatedTodo.doneStatus);

        // issue a get request on the to do, just to double check it updated
        final Todos getTodo = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(updatedDetails).
                get(apiPath("/todos/" + todo.id)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response().as(Todos.class);

        Assertions.assertEquals(updatedDetails.id, getTodo.todos.get(0).id);
        Assertions.assertEquals(updatedDetails.title, getTodo.todos.get(0).title);
        Assertions.assertEquals(updatedDetails.description, getTodo.todos.get(0).description);
        Assertions.assertEquals(updatedDetails.doneStatus, getTodo.todos.get(0).doneStatus);

    }

}
