package uk.co.compendiumdev.challenger.restassured._03_get_challenges;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class C005GetTodosIdFound200Test extends RestAssuredBaseTest {


    @Test
    void canGetSpecificTodo(){

        // since tests can run in any order, we should create the todoinstance, to guarantee we can get it
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

}
