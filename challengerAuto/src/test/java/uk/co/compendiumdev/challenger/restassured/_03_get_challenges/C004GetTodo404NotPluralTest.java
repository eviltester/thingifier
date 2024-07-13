package uk.co.compendiumdev.challenger.restassured._03_get_challenges;


import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C004GetTodo404NotPluralTest extends RestAssuredBaseTest {


    @Test
    void can404WhenNotPluralNouns(){

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                get(apiPath( "/todo")).
                then().
                statusCode(404);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();

        Assertions.assertTrue(statuses.getChallengeNamed("GET /todo (404) not plural").status);
    }

}
