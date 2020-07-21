package uk.co.compendiumdev.challenger.restassured;


import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;


public class CanHeadTodosTest extends RestAssuredBaseTest {


    @Test
    void canCheckHeadForTodos(){

        final Response headresponse = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                head(apiPath( "/todos")).
                then().
                    statusCode(200).
                and().extract().response();

        // pass challenge
        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("HEAD /todos (200)").status);


        Assertions.assertTrue(headresponse.body().asString().equals(""),
                        "Expected no Body for Head response");

        final Response todosgetresponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos")).
                then().
                statusCode(200).
                and().extract().response();

        // headers should be the same for get and head
        Assertions.assertEquals(headresponse.headers().size(),
                                todosgetresponse.headers().size());

    }

}
