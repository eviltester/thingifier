package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class CanGetTodosTest extends RestAssuredBaseTest {

    @Test
    void canGetTodos(){

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos")).
            then().
                statusCode(200).
                contentType(ContentType.JSON);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200)").status);

    }

    @Test
    void canGetSpecificTodo(){

        // since tests can run in any order, we should create the todo, to guarantee we can get it
        TodosApi api = new TodosApi();
        String name = "get me " + System.currentTimeMillis();
        final Todo todo = api.createTodo(name, "", false);

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos/" + todo.id)).
            then().
                statusCode(200).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos/{id} (200)").status);

    }

    @Test
    void can404WhenGetSpecificTodo(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos/9999999")).
                then().
                statusCode(404).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos/{id} (404)").status);
    }

    @Test
    void can404WhenNotPluralNouns(){

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                get(apiPath( "/todo")).
            then().
                statusCode(404);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("GET /todo (404) not plural").status);
    }

}
