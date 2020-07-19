package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;

public class CanClearEnvironmentTest {

    /*
     * A quick set of test methods to replicate the automated execution in Postman to allow compare and contrast
       https://github.com/eviltester/thingifier/blob/master/docs/rest_testing/TodoManagerThingifier.postman_collection.json
     */

    @BeforeAll
    public static void clearDataFromEnv(){
        when().post(Environment.getEnv("/admin/data/thingifier")).then().statusCode(200);

        final JsonPath clearedData = when().get(Environment.getEnv("/todos")).then().statusCode(200).extract().body().jsonPath();
        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assertions.assertEquals(0, newNumberOfTodos);
    }


    @Test
    public void shouldBeNoTodos(){

        Assertions.assertEquals(0, when().get(Environment.getEnv("/todos")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("todos").size());
    }

    @Test
    public void shouldBeNoProjects(){

        Assertions.assertEquals(0, when().get(Environment.getEnv("/projects")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("projects").size());

    }

    @Test
    public void shouldBeNoCategories(){


        Assertions.assertEquals(0, when().get(Environment.getEnv("/categories")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("categories").size());
    }
}
