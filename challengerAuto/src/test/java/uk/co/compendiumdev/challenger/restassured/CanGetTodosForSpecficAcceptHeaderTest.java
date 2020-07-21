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
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) JSON").status);

        // should be able to parse with GSON if JSON response
        new Gson().fromJson(response.body().asString(), Todos.class);

    }

    @Test
    void canGetTodosAsXML(){

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) XML").status);

        // XML in response
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        Assertions.assertTrue(response.body().asString().contains("</todos>"));

    }

    @Test
    void canGetTodosAsAny(){

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("*/*").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) ANY").status);

        // should be able to parse with GSON if JSON response by default
        new Gson().fromJson(response.body().asString(), Todos.class);

    }

    @Test
    void canGetTodosAsPreferredXML(){

        // ask for multiple but prefer xml
        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml, application/json").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) XML pref").status);

        // XML in response
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        Assertions.assertTrue(response.body().asString().contains("</todos>"));

    }

    @Test
    void canGetTodosAsJSONWhenNoAcceptHeaderSent(){

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("").  // best I can do with RestAssured, setting header to "" - includes header but with no value
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) no accept").status);

        // should be able to parse with GSON if JSON response
        new Gson().fromJson(response.body().asString(), Todos.class);
    }

    @Test
    void canGet406WhenUnsupportedAcceptHeaderSent(){

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/gzip").
                get(apiPath("/todos")).
                then().
                statusCode(406).
                contentType(ContentType.JSON).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (406)").status);

    }
}
