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

public class ProjectTodoRelationshipCrudTest {

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
    public void canCreateAndAmendSequence(){

        // CREATE Project WITH POST

        final HashMap<String, String> givenBody = new HashMap<>();
        givenBody.put("title", "a specific project");

        Response projectResponse = given().body(givenBody).contentType("application/json").
                when().post("/projects").
                then().
                statusCode(201).
                contentType(ContentType.JSON).and().extract().response();

        String specificProjectId = projectResponse.getBody().jsonPath().get("id");


        // CREATE A To do

        final HashMap<String, String> givenTodoBody = new HashMap<>();
        givenTodoBody.put("title", "a specific todo");

        Response todoResponse = given().body(givenTodoBody).contentType("application/json").
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).and().extract().response();

        String specificTodoId = todoResponse.getBody().jsonPath().get("id");

        // Create a specific project to do relationship with POST
        final HashMap<String, String> givenRefBody = new HashMap<>();
        givenRefBody.put("id", specificTodoId);

        given().body(givenRefBody).contentType("application/json").
                when().post("/projects/" + specificProjectId + "/tasks").
                then().
                statusCode(201).
                contentType(ContentType.JSON);

        // GET todos for a project

        Response response = when().get("/projects/" + specificProjectId + "/tasks").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertEquals(1, response.getBody().jsonPath().getList("todos").size());
        Assertions.assertEquals(specificTodoId, response.getBody().jsonPath().get("todos[0].id"));
        // if non-compressed relationship rendering is used then this path
        Assertions.assertEquals(specificProjectId, response.getBody().jsonPath().get("todos[0].relationships[0].tasksof[0].projects[0].id"));
        //Assertions.assertEquals(specificProjectGuid, response.getBody().jsonPath().get("todos[0].task-of[0].guid"));


        // DELETE the Project Todos Relationship TO DO
        when().delete("/projects/" + specificProjectId + "/tasks/" +specificTodoId).
                then().
                statusCode(200).
                contentType(ContentType.JSON);

        // GET todos for a project

        response = when().get("/projects/" + specificProjectId + "/tasks").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertEquals(0, response.getBody().jsonPath().getList("todos").size());

        // GET project and check no relationships listed
        response = when().get("/projects/" + specificProjectId).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertNull(response.getBody().jsonPath().get("projects[0].relationships"));

        // DELETE the Project Todos Relationship TO DO again
        when().delete("/projects/" + specificProjectId + "/tasks/" +specificTodoId).
                then().
                statusCode(404).
                contentType(ContentType.JSON);
    }

}
