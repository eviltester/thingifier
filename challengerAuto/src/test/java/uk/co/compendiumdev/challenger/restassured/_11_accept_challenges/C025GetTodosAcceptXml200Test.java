package uk.co.compendiumdev.challenger.restassured._11_accept_challenges;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.Arrays;
import java.util.List;

public class C025GetTodosAcceptXml200Test extends RestAssuredBaseTest {


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

}
