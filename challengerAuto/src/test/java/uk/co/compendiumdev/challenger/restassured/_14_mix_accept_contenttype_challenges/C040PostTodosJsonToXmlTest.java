package uk.co.compendiumdev.challenger.restassured._14_mix_accept_contenttype_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C040PostTodosJsonToXmlTest extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithJSONAcceptingXMLPost(){

        Todo createMe = new Todo();
        createMe.title = "json name " + System.currentTimeMillis();
        createMe.description = "json description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Todo todo = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.XML).
                extract().response().as(Todo.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos JSON to XML").status);

        Assertions.assertEquals(createMe.title, todo.title);
        Assertions.assertEquals(createMe.description, todo.description);
        Assertions.assertEquals(createMe.doneStatus, todo.doneStatus);

    }


}
