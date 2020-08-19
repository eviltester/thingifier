package uk.co.compendiumdev.challenger.restassured.defects;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.sparkstart.Environment;

public class NullPointerAuthenticationTokensTest extends RestAssuredBaseTest {

    String validToken = "";

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostTokenCausedNullPointer(){

        // in single player mode this test will 201 because we don't need challenger header
        int expectedResponse=401;
        if(Environment.SINGLE_PLAYER_MODE){
            expectedResponse=201;
        }

        validToken =  RestAssured.
            given().
//                header("X-CHALLENGER", xChallenger).
//                accept("application/json").
//                contentType("application/json").
                auth().preemptive().basic("admin","password").
            when().
                post(apiPath("/secret/token")).
            then().
                statusCode(expectedResponse).   // was 400 when defect was in code
            extract().
                header("X-AUTH-TOKEN");
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderGetNoteCausedNullPointer(){

        validToken =  RestAssured.
                given().
//                header("X-CHALLENGER", xChallenger).
//                accept("application/json").
//                contentType("application/json").
                    auth().preemptive().basic("admin","password").
                when().
                    get(apiPath("/secret/note")).
                then().
                        statusCode(401).   // was 400 when defect was in code
                extract().
                        header("X-AUTH-TOKEN");
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostNoteCausedNullPointer(){

        validToken =  RestAssured.
                given().
//                header("X-CHALLENGER", xChallenger).
//                accept("application/json").
//                contentType("application/json").
                    auth().preemptive().basic("admin","password").
                when().
                        post(apiPath("/secret/note")).
                then().
                        statusCode(401).   // was 400 when defect was in code
                extract().
                        header("X-AUTH-TOKEN");
    }


}



