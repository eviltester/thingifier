package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;
import uk.co.compendiumdev.sparkstart.Environment;

import java.io.File;

public class AgainstSparkAppBaseTest {

    static String environment="";

    public static void enableEnv(){
        environment = Environment.getBaseUri();
    }

    public String apiPath(final String path) {
        return environment + path;
    }
}
