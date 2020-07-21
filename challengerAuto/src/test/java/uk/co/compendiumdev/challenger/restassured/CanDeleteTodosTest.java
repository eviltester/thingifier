package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.List;

public class CanDeleteTodosTest extends RestAssuredBaseTest {

    @Test
    void canDeleteATodoItem(){

        TodosApi api = new TodosApi();

        Todo created = api.createTodo("my new todo",
                    "my description",
                    true);

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                delete(apiPath( "/todos/" + created.id)).
            then().
                statusCode(200).
                contentType(ContentType.JSON);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("DELETE /todos/{id} (200)").status);

        // check it was actually deleted
        RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/todos/" + created.id)).
                then().
                statusCode(404);
    }

    @Test
    void canDeleteAllTodoItems(){

        TodosApi api = new TodosApi();

        List<Todo> todos = api.getTodos();

        for(Todo todo : todos) {

            RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                    delete(apiPath("/todos/" + todo.id)).
                then().
                    statusCode(200).
                    contentType(ContentType.JSON);
        }

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("DELETE /todos/{id} (200) all").status);

        // check all deleted
        List<Todo> notodos = api.getTodos();
        Assertions.assertEquals(0, notodos.size());
    }

}
