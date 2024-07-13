package uk.co.compendiumdev.challenger.restassured._19_misc_challenges;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.ArrayList;
import java.util.List;

public class C059AddMaximumNumberOfTodosTest extends RestAssuredBaseTest {

    @Test
    void canCreateAllTodosAndMaxOutTheLimitSimplified(){

        TodosApi todos = new TodosApi();

        // get todos then count them and add only the expected number
        List<Todo> currentTodos = todos.getTodos();

        int todosToCreate = 20 - currentTodos.size();

        // this would also allow us to delete some here if they were already at the max

        List<Integer> idsToDelete = new ArrayList<>();


        while( todosToCreate > 0 ){
            Todo aTodo = todos.createTodo("my title " + todosToCreate, "description", true);
            idsToDelete.add(aTodo.id);
            todosToCreate--;
        };

        // create a to do to throw it over the edge
        Todo createMe = new Todo();
        createMe.title = "my title";
        createMe.description = "my description";

        Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(400).
                extract().response();

        ErrorMessages messages = response.as(ErrorMessages.class);

        Assertions.assertTrue(messages.errorMessages.contains("ERROR: Cannot add instance, maximum limit of 20 reached"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201) all").status);

        // now delete those todos we created
        idsToDelete.forEach(todos::deleteTodo);
    }


}
