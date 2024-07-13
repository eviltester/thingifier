package uk.co.compendiumdev.challenger.restassured._13_restore_session_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.ArrayList;

public class C038PutRestoreChallengerTodosTest extends RestAssuredBaseTest {

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
