package uk.co.compendiumdev.challenger.restassured;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.Map;

public class CanGetChallengesTest extends RestAssuredBaseTest {


    @Test
    void canGetChallenges(){

        // challenges should be set as soon as we get it - no need for multiple calls
        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/challenges")).
                then().
                statusCode(200).and().extract().response();

        // challenge should be met
        // this fails on RestAssured 4.3.1 - downgraded to 4.2.0
        Map<String, Object> challenge = response.body().jsonPath().
                            get("challenges.find { it.name == 'GET /challenges (200)' }");

        Assertions.assertTrue((Boolean)challenge.get("status"));
    }

}
