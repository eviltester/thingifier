package uk.co.compendiumdev.challenger.restassured._11_accept_challenges;


import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C030GetTodosNotAcceptableHeader406Test extends RestAssuredBaseTest {


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
