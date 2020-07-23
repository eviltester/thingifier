package uk.co.compendiumdev.challenger.restassured;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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

public class CanCreateTodosWithPOSTTest extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Response response = RestAssured.
                given().
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
                "todos/" + createdTodo.id,
                    response.header("Location"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201)").status);

    }

    @Test
    void can400CreateATodoWithInvalidDoneStatusPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();

        // cannot create an invalid status with an invalid boolean value so...
        // createMe.doneStatus = true;
        // work with the JSON to create a 'bad' payload

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("doneStatus", "truthy");


        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
            accept("application/json").
            contentType("application/json").
            body(createMeJson.toString()).
            post(apiPath( "/todos")).
            then().
            statusCode(400).
            contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) doneStatus").status);

    }

    @Test
    void can415CreateATodoWithInvalidContentType(){

        Todo todo = new Todo();
        todo.doneStatus = true;
        todo.title = "invalid content type";
        todo.description = "invalid content";

        String payload = new Gson().toJson(todo);

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType(ContentType.BINARY).
                body(payload.getBytes()).
                post(apiPath( "/todos")).
                then().
                statusCode(415).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (415)").status);

    }

    @Test
    void canCreateATodoWithXMLPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        // if not using RestAssured to convert objects then create String payloads
//        String xml = String.format(
//                "<todo><title>%s</title><description>%s</description>" +
//                        "<doneStatus>%b</doneStatus></todo>",
//                createMe.title, createMe.description, createMe.doneStatus);

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml").
                contentType("application/xml").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.XML).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos XML").status);


        // GET on Location header to return the to do and check values
        String locationHeader = response.getHeader("Location");
        String pattern = "todos/(.*)";
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
        String pattern = "todos/(.*)";
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
