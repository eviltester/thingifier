package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import uk.co.compendiumdev.sparkstart.Environment;

import java.io.File;

public class RestAssuredBaseTest {

    static String environment="";
    public static String xChallenger = "";

    @BeforeAll
    static void enableEnv(){
        environment = Environment.getBaseUri();

        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter());

        if(xChallenger==""){

            // because we are unit tests we are running in single player mode so delete the single player data
            try {
                final File dataFile = new File(System.getProperty("User.dir"), "rest-api-challenges-single-player.data.txt");
                if(dataFile.exists()){
                    dataFile.delete();
                }
            }catch(Exception e){

            }

            xChallenger = RestAssured.
                given().
                    post(Environment.getEnv( "/challenger")).
                then().
                    statusCode(201).
                extract().
                    header("X-CHALLENGER");
        }
    }

    public String apiPath(final String path) {
        return environment + path;
    }

}
