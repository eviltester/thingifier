package uk.co.compendiumdev.challenger.restassured.defects;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChallengerNotLoadedTest extends RestAssuredBaseTest {

    String validToken = "";

    @Test
    public void challengerNotLoadedWhenSkipCallToGetChallengers() throws IOException {

        //have challenger data predefined but not in memory
        // 2ce954c6-caa1-4299-85af-205a9d9f7867.data
        String notLoadedChallengerGUID = "2ce954c6-caa1-4299-85af-205a9d9f7867";
        final File resourceFile = new File(System.getProperty("user.dir") , "/src/test/resources/" + notLoadedChallengerGUID + ".data.txt");
        final File dataFile = new File(System.getProperty("user.dir") , notLoadedChallengerGUID + ".data.txt");

        if(!resourceFile.exists()){
            Assertions.fail("Could not find data file " + notLoadedChallengerGUID);
        }

        if(dataFile.exists()){
            dataFile.delete();
        }

        Files.copy(resourceFile.toPath(), dataFile.toPath());

        // force the challenger data to be loaded when 'get' challenger, not just in the routing
        validToken =  RestAssured.
            given().
                header("X-CHALLENGER", notLoadedChallengerGUID).
//                accept("application/json").
//                contentType("application/json").
                auth().preemptive().basic("admin","password").
            when().
                post(apiPath("/secret/token")).
            then().
                statusCode(201).   // was 401 when defect was in code
            extract().
                header("X-AUTH-TOKEN");
    }


}



