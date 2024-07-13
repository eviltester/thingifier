package uk.co.compendiumdev.challenger.restassured._05_post_creation_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C009PostTodos200Test extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response();

        Todo createdTodo = response.body().as(Todo.class);

        Assertions.assertNotSame(createMe, createdTodo);
        Assertions.assertEquals(createMe.title, createdTodo.title);
        Assertions.assertEquals(createMe.description, createdTodo.description);
        Assertions.assertEquals(createMe.doneStatus, createdTodo.doneStatus);

        // not much I can check on the id
        Assertions.assertNotNull(createdTodo.id);
        Assertions.assertTrue(createdTodo.id>0);

        // GET on Location header should return the to do location but just check the format
        Assertions.assertEquals(
                "/todos/" + createdTodo.id,
                response.header("Location"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201)").status);

    }

}