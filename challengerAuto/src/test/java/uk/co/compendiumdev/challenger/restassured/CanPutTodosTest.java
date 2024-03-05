package uk.co.compendiumdev.challenger.restassured;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;

public class CanPutTodosTest extends RestAssuredBaseTest {

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


    @Test
    void canAmendATodoWithPut(){

        Todo amendMe = new TodosApi().getOrCreateAnyExistingTodo();

        // amendMe.id = - cannot amend the id as it is auto assigned
        amendMe.title = "my name " + System.currentTimeMillis(); // title is mandatory and must be in the message
        amendMe.description = "my description " + System.currentTimeMillis(); // if not present default "" will be set
        amendMe.doneStatus = true; // if not present then default false will be set

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(amendMe).
                put(apiPath("/todos/" + amendMe.id)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();

        Todo amendedTodo = response.body().as(Todo.class);

        Assertions.assertEquals(amendMe.id, amendedTodo.id);
        Assertions.assertEquals(amendMe.title, amendedTodo.title);
        Assertions.assertEquals(amendMe.description, amendedTodo.description);
        Assertions.assertEquals(amendMe.doneStatus, amendedTodo.doneStatus);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /todos/{id} full (200)").status);
    }

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

    @Test
    void canFailToAmendATodoDueToMissingTitle(){

        Todo amendMe = new TodosApi().getOrCreateAnyExistingTodo();

        // amendMe.id = - cannot amend the id as it is auto assigned
        // title is mandatory and must be in the message

        final JsonElement amendTodoJson = new Gson().toJsonTree(amendMe);
        amendTodoJson.getAsJsonObject().
                remove("title");

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(amendTodoJson.toString()).
                put(apiPath("/todos/" + amendMe.id)).
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                extract().response();

        ErrorMessages error = response.body().as(ErrorMessages.class);
        Assertions.assertTrue(error.errorMessages.get(0).contains("title : field is mandatory"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /todos/{id} no title (400)").status);
    }

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

    @Test
    public void canGetAllTodosAndPutTodosToRestore(){

        Assertions.assertNotNull(xChallenger);
        Assertions.assertTrue(xChallenger.length()>5);

        Todos todosResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/challenger/database/" + xChallenger)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).and().extract().response().as(Todos.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /challenger/database/guid (200)").status,
                "expected challenge not passed");



        Todos newTodos = new Todos();
        newTodos.todos = new ArrayList<>();
        Todo aTodo = new Todo();
        aTodo.id = 100;
        aTodo.title="this todo put here by me";
        newTodos.todos.add(aTodo);

        // create it with PUT using mostly previous values
        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                body(newTodos).
                put(apiPath("/challenger/database/" + xChallenger)).
                then().
                statusCode(204). // updated existing
                contentType(ContentType.JSON);


        ChallengesStatus statusesCheck = new ChallengesStatus();
        statusesCheck.getFor(xChallenger);
        Assertions.assertTrue(statusesCheck.getChallengeNamed("PUT /challenger/database/guid (Update)").status,
                    "expected put challenge not passed");

        Todos amendedTodosResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/challenger/database/" + xChallenger)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).and().extract().response().as(Todos.class);

        Assertions.assertEquals(1, amendedTodosResponse.todos.size());
        Assertions.assertEquals("this todo put here by me", amendedTodosResponse.todos.get(0).title);
    }

}
