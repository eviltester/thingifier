package uk.co.compendiumdev.challenger.restassured._19_misc_challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.List;

public class C058DeleteAllTodosTest extends RestAssuredBaseTest {

    @Test
    void canDeleteAllTodoItems(){

        TodosApi api = new TodosApi();

        List<Todo> todos = api.getTodos();

        for(Todo todo : todos) {
            api.deleteTodo(todo.id);
        }

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("DELETE /todos/{id} (200) all").status);

        // check all deleted
        List<Todo> noToDos = api.getTodos();
        Assertions.assertEquals(0, noToDos.size());
    }
}
