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

        String specificProjectId = "1";
        String specificTodoId = "1";

        // CREATE Project WITH PUT

        final HashMap<String, String> givenBody = new HashMap<String, String>();
        givenBody.put("title", "a specific project");

        given().body(givenBody).
                when().put("/projects/" + specificProjectId).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                header("Location", "projects/" + specificProjectId).
                header("X-Thing-Instance-Primary-Key", specificProjectId);

        // CREATE A Specific To do

        given().body(givenBody).
                when().put("/todos/" + specificTodoId).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                header("Location", "todos/" + specificTodoId).
                header("X-Thing-Instance-Primary-Key", specificTodoId);

        // Create a specific project to to do relationship with POST
        givenBody.put("id", specificTodoId);

        given().body(givenBody).
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
