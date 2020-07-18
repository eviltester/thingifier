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
                accept("application/json").
                get(apiPath( "/challenges"));

        // challenge should be met
        Assertions.assertEquals(200, response.getStatusCode());
        Map<String, Object> challenge = response.body().jsonPath().
                            get("challenges.find { it.name == 'GET /challenges (200)' }");

        Assertions.assertTrue((Boolean)challenge.get("status"));
    }

}
