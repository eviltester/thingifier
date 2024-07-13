package uk.co.compendiumdev.challenger.restassured._12_contenttype_challenges;

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

public class C032PostTodosJsonTest extends RestAssuredBaseTest {


    @Test
    void canCreateATodoWithJSONPost(){

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

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos JSON").status);


        // GET on Location header to return the to do and check values
        String locationHeader = response.getHeader("Location");
        String pattern = "/todos/(.*)";
        Pattern getId = Pattern.compile(pattern);
        Matcher matcher = getId.matcher(locationHeader);
        matcher.find();

        String id = matcher.group(1);
        final TodosApi api = new TodosApi();
        final Todo created = api.getTodo(id);

        Assertions.assertEquals(createMe.title, created.title);
        Assertions.assertEquals(createMe.description, created.description);
        Assertions.assertEquals(createMe.doneStatus, created.doneStatus);

    }


}