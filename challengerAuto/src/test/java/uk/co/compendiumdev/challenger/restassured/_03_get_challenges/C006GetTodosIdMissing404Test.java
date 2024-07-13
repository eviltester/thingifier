package uk.co.compendiumdev.challenger.restassured._03_get_challenges;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C006GetTodosIdMissing404Test extends RestAssuredBaseTest {


    @Test
    void can404WhenGetSpecificTodo(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos/9999999")).
                then().
                statusCode(404).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos/{id} (404)").status);
    }

}
