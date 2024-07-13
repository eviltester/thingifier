package uk.co.compendiumdev.challenger.restassured._14_mix_accept_contenttype_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class C039PostTodosXmlToJsonTest extends RestAssuredBaseTest {


    @Test
    void canCreateATodoWithXMLAcceptingJsonPost(){

        Todo createMe = new Todo();
        createMe.title = "xml name " + System.currentTimeMillis();
        createMe.description = "xml description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Todo todo = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/xml").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response().as(Todo.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos XML to JSON").status);

        Assertions.assertEquals(createMe.title, todo.title);
        Assertions.assertEquals(createMe.description, todo.description);
        Assertions.assertEquals(createMe.doneStatus, todo.doneStatus);

    }

}