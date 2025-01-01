package uk.co.compendiumdev.challenger.restassured._16_http_method_override_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class CXXXextraDeleteExistingTodo200Test extends RestAssuredBaseTest {

    @Test
    void canDeleteATodoItem(){

        TodosApi api = new TodosApi();

        Todo created = api.createTodo("my new todo",
                    "my description",
                    true);

        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
                header("X-HTTP-Method-Override", "DELETE").
                accept("application/json").
                post(apiPath( "/todos/" + created.id)).
            then().
                statusCode(200).
                contentType(ContentType.JSON);

        // check it was actually deleted
        RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/todos/" + created.id)).
                then().
                statusCode(404);
    }

}
