package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
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


    @Test
    public void canCreateAndAmendSequence(){

        String specificProjectGuid = "4c656319-1e4c-4286-8c2c-dff2d6762f0d";
        String specificTodoGuid = "09452402-32de-4403-8e4a-a27bc333448c";

        // CREATE Project WITH PUT

        final HashMap<String, String> givenBody = new HashMap<String, String>();
        givenBody.put("title", "a specific project");

        given().body(givenBody).
                when().put("/projects/" + specificProjectGuid).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                header("Location", "projects/" + specificProjectGuid).
                header("X-Thing-Instance-GUID", specificProjectGuid);

        // CREATE A Specific To do

        given().body(givenBody).
                when().put("/todos/" + specificTodoGuid).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                header("Location", "todos/" + specificTodoGuid).
                header("X-Thing-Instance-GUID", specificTodoGuid);

        // Create a specific project to to do relationship with POST
        givenBody.put("guid", specificTodoGuid);

        given().body(givenBody).
                when().post("/projects/" + specificProjectGuid + "/tasks").
                then().
                statusCode(201).
                contentType(ContentType.JSON);

        // GET todos for a project

        Response response = when().get("/projects/" + specificProjectGuid + "/tasks").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertEquals(1, response.getBody().jsonPath().getList("todos").size());
        Assertions.assertEquals(specificTodoGuid, response.getBody().jsonPath().get("todos[0].guid"));
        // if non-compressed relationship rendering is used then this path
        Assertions.assertEquals(specificProjectGuid, response.getBody().jsonPath().get("todos[0].relationships[0].task-of[0].projects[0].guid"));
        //Assertions.assertEquals(specificProjectGuid, response.getBody().jsonPath().get("todos[0].task-of[0].guid"));


        // DELETE the Project Todos Relationship TO DO
        when().delete("/projects/" + specificProjectGuid + "/tasks/" +specificTodoGuid).
                then().
                statusCode(200).
                contentType(ContentType.JSON);

        // GET todos for a project

        response = when().get("/projects/" + specificProjectGuid + "/tasks").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertEquals(0, response.getBody().jsonPath().getList("todos").size());

        // GET project and check no relationships listed
        response = when().get("/projects/" + specificProjectGuid).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        Assertions.assertNull(response.getBody().jsonPath().get("projects[0].relationships"));

        // DELETE the Project Todos Relationship TO DO again
        when().delete("/projects/" + specificProjectGuid + "/tasks/" +specificTodoGuid).
                then().
                statusCode(404).
                contentType(ContentType.JSON);
    }

}
