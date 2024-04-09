package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class TodosCrudTest {

    // replicate the automated execution and assertion from Postman project
    // https://github.com/eviltester/thingifier/blob/master/docs/rest_testing/TodoManagerThingifier.postman_collection.json


    @BeforeAll
    public static void clearDataFromEnv(){

        // avoid the use of Environment.getEnv("/todos") etc. to keep code a little clearer
        RestAssured.baseURI = Environment.getBaseUri();

        when().post("/admin/data/thingifier")
                    .then().statusCode(200);

        final JsonPath clearedData = when().get("/todos")
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assertions.assertEquals(0, newNumberOfTodos);
    }

    @AfterAll
    public static void shutItDown(){
        Environment.stop();
    }

    @Test
    public void cannotCreateWithInvalidTodoBlankTitlePosts(){

        //{"title": "a specific to do Title"}
        final HashMap<String, String> givenBody = new HashMap<>();
        givenBody.put("title", "");

        final JsonPath body = given().body(givenBody).contentType("application/json").
                when().post("/todos").
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();

        String errorMessages = (String)body.getList("errorMessages").get(0);
        Assertions.assertTrue(errorMessages.contains("title : can not be empty"),errorMessages);
    }

    @Test
    public void cannotCreateWithInvalidTodoBlankBodyPosts(){


        final JsonPath body = given().body("{}").contentType("application/json").
                when().post("/todos").
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();

        Assertions.assertEquals("title : field is mandatory",
                body.getList("errorMessages").get(0));
    }

    @Test
    public void cannotCreateWithInvalidTodoNoBodyPosts(){


        final JsonPath body = given().body("").contentType("application/json").
                when().post("/todos").
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();

        Assertions.assertEquals("title : field is mandatory",
                body.getList("errorMessages").get(0));
    }

    @Test
    public void canCreateWithPost(){

        final HashMap<String, String> givenBody = new HashMap<>();
        givenBody.put("title", "a specific todo Title");

        final Response response = given().body(givenBody).contentType("application/json").
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).and().extract().response();

        Assertions.assertEquals(
                        response.header("Location"),
                "/todos/" + response.header("X-Thing-Instance-Primary-Key"));

        final JsonPath body = response.jsonPath();

        Assertions.assertEquals("false", body.get("doneStatus"));
        Assertions.assertEquals(response.header("X-Thing-Instance-Primary-Key"), body.get("id"));
        Assertions.assertEquals("", body.get("description"));
        Assertions.assertEquals("a specific todo Title", body.get("title"));
    }

    @Test
    public void canCreateAndAmendSequence(){

        // CREATE WITH POST

        final HashMap<String, String> givenBody = new HashMap<>();
        givenBody.put("title", "a specific todo Title for put");

        JsonPath body = given().body(givenBody).contentType("application/json").
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();

        Assertions.assertEquals("false", body.get("doneStatus"));
        String specificId = body.get("id");
        Assertions.assertEquals("", body.get("description"));
        Assertions.assertEquals("a specific todo Title for put", body.get("title"));


        // AMEND with PUT
        givenBody.put("title", "a put amended specific todo Title for put");

        Response response = given().body(givenBody).contentType("application/json").
                when().put("/todos/" + specificId).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();


        Assertions.assertFalse(response.headers().hasHeaderWithName("Location"));
        Assertions.assertFalse(response.headers().hasHeaderWithName("X-Thing-Instance-Primary-Key"));

        body = response.getBody().jsonPath();

        Assertions.assertEquals("false", body.get("doneStatus"));
        Assertions.assertEquals(specificId, body.get("id"));
        Assertions.assertEquals("", body.get("description"));
        Assertions.assertEquals("a put amended specific todo Title for put", body.get("title"));


        // AMEND with POST
        givenBody.put("title", "a specific todo Title Amended");

        response = given().body(givenBody).contentType("application/json").
                when().post("/todos/" + specificId).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();


        Assertions.assertFalse(response.headers().hasHeaderWithName("Location"));
        Assertions.assertFalse(response.headers().hasHeaderWithName("X-Thing-Instance-Primary-Key"));

        body = response.getBody().jsonPath();

        Assertions.assertEquals("false", body.get("doneStatus"));
        Assertions.assertEquals(specificId, body.get("id"));
        Assertions.assertEquals("", body.get("description"));
        Assertions.assertEquals("a specific todo Title Amended", body.get("title"));

        // GET the TO DO item
        response = when().get("/todos/" + specificId).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertFalse(response.headers().hasHeaderWithName("Location"));
        Assertions.assertFalse(response.headers().hasHeaderWithName("X-Thing-Instance-Primary-Key"));

        body = response.getBody().jsonPath();

        Assertions.assertEquals("false", body.get("todos[0].doneStatus"));
        Assertions.assertEquals(specificId, body.get("todos[0].id"));
        Assertions.assertEquals("", body.get("todos[0].description"));
        Assertions.assertEquals("a specific todo Title Amended", body.get("todos[0].title"));


        // DELETE the TO DO item
        when().delete("/todos/" + specificId).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        // Cannot GET a deleted to do item
        response = when().get("/todos/" + specificId).
                then().
                statusCode(404).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertEquals("Could not find an instance with todos/" + specificId,
                response.getBody().jsonPath().get("errorMessages[0]"));
    }

}
