package uk.co.compendiumdev.challenger.restassured;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class CanGetTodosForSpecficAcceptHeaderTest extends RestAssuredBaseTest {

    @Test
    void canGetTodosAsJSON(){

        final Response response = RestAssured.
                given().
                accept("application/json").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) JSON").status);

        // should be able to parse with GSON if JSON repsonse
        new Gson().fromJson(response.body().asString(), Todos.class);

    }

    @Test
    void canGetTodosAsXML(){

        final Response response = RestAssured.
                given().
                accept("application/xml").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) XML").status);

        // should be able to parse with GSON if JSON repsonse
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        Assertions.assertTrue(response.body().asString().contains("</todos>"));

    }


}
