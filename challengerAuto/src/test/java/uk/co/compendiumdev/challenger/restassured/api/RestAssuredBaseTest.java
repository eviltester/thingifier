package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import uk.co.compendiumdev.sparkstart.Environment;

public class RestAssuredBaseTest {

    static String environment="";
    public static String xChallenger = "";

    @BeforeAll
    static void enableEnv(){
        Assumptions.assumeTrue(
                Environment.shouldRunFullSuite(),
                Environment.fullSuiteSkipReason());

        environment = Environment.getBaseUri();

        // switch on logging for RestAssured requests
//        RestAssured.filters(
//                new RequestLoggingFilter(),
//                new ResponseLoggingFilter());

        //RestAssured.proxy("localhost",8888);

        if(xChallenger.isEmpty()){
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
