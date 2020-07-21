package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class CanGetFilteredTodosTest extends RestAssuredBaseTest {

    @Test
    void canGetFilteredTodos() {

        // create a todo for true and a todo for false to ensure test passes
        TodosApi api = new TodosApi();
        api.createTodo("not done", "this todo is not done", false);
        final Todo doneTodo = api.createTodo("done", "this todo is done", true);

        Todos todosList = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/todos?doneStatus=true")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).extract().body().as(Todos.class);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) ?filter").status);

        // check results after doing challenge

        boolean foundOurTodo = false;
        boolean foundAllTrue = true;
        for (Todo todo : todosList.todos) {
            if (todo.id == doneTodo.id) {
                foundOurTodo = true;
            }
            foundAllTrue = foundAllTrue && todo.doneStatus;
        }

        Assertions.assertTrue(foundOurTodo,
                "Expected to see the todo we created as 'done' in the list");
        Assertions.assertTrue(foundAllTrue,
                "Expected all todos returned to be 'done'");
    }

    /* exercises:
        - filter on multiple fields
        - filter on false to get to do items still requiring action
     */
}