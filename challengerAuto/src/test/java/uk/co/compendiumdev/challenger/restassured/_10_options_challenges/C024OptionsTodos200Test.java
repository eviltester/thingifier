package uk.co.compendiumdev.challenger.restassured._10_options_challenges;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.Arrays;
import java.util.List;

public class C024OptionsTodos200Test extends RestAssuredBaseTest {


    @Test
    void canCheckOptionsForTodos(){

        final Response response = RestAssured.
                given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                options(apiPath( "/todos")).
                then().
                // this should really be 204, but to keep the challenge docs etc.
                // backwards compatible there is a hack in the hook to return
                // 200 for this option request
                    statusCode(200).
                and().extract().response();

        // pass challenge
        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("OPTIONS /todos (200)").status);


        // should have Allow Header
        final String allow = response.header("Allow");
        final String[] allowed = allow.replace(" ","").split(",");
        List<String> checkAllowed = Arrays.asList(allowed);

        Assertions.assertTrue(checkAllowed.contains("OPTIONS"), "Should Allow OPTIONS");
        Assertions.assertTrue(checkAllowed.contains("GET"), "Should Allow GET");
        Assertions.assertTrue(checkAllowed.contains("HEAD"), "Should Allow HEAD");
        Assertions.assertTrue(checkAllowed.contains("POST"), "Should Allow POST");
    }

}
