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

public class CreateALotOfTodosTest {

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



        // CREATE todos WITH POST

        final HashMap<String, String> givenBody = new HashMap<String, String>();

        for(int i=0; i<100; i++){
            givenBody.put("title", "a title " + i);

            given().body(givenBody).
                    when().post("/todos").
                    then().
                    statusCode(201).
                    contentType(ContentType.JSON);
        }


        final Response response = when().get("/todos")
                .then().statusCode(200).extract().response();

        final JsonPath clearedData = response.getBody().jsonPath();
        final int newNumberOfTodos = clearedData.getList("todos").size();
        Assertions.assertEquals(100, newNumberOfTodos);

        System.out.println(response.getBody().prettyPrint());


    }

}
