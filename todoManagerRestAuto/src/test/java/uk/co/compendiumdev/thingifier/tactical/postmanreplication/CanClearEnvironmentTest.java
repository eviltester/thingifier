package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.path.json.JsonPath;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.when;

public class CanClearEnvironmentTest {

    /*
     * A quick set of test methods to replicate the automated execution in Postman to allow compare and contrast
       https://github.com/eviltester/thingifier/blob/master/docs/rest_testing/TodoManagerThingifier.postman_collection.json
     */

    @BeforeClass
    public static void clearDataFromEnv(){
        when().post(Environment.getEnv("/admin/data/thingifier")).then().statusCode(200);

        final JsonPath clearedData = when().get(Environment.getEnv("/todos")).then().statusCode(200).extract().body().jsonPath();
        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assert.assertEquals(0, newNumberOfTodos);
    }


    @Test
    public void shouldBeNoTodos(){

        Assert.assertEquals(0, when().get(Environment.getEnv("/todos")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("todos").size());
    }

    @Test
    public void shouldBeNoProjects(){

        Assert.assertEquals(0, when().get(Environment.getEnv("/projects")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("projects").size());

    }

    @Test
    public void shouldBeNoCategories(){


        Assert.assertEquals(0, when().get(Environment.getEnv("/categories")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("categories").size());
    }
}
