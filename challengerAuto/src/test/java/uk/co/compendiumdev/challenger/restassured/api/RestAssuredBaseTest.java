package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;
import uk.co.compendiumdev.sparkstart.Environment;

public class RestAssuredBaseTest {

    static String environment="";

    @BeforeAll
    static void enableEnv(){
        environment = Environment.getBaseUri();

        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter());
    }

    public String apiPath(final String path) {
        return environment + path;
    }

}
