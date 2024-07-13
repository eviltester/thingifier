package uk.co.compendiumdev.challenger.restassured._03_get_challenges;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C003GetTodos200Test extends RestAssuredBaseTest {


    @Test
    void canGetTodos(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON);


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200)").status);

    }

}
