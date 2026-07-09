package uk.co.compendiumdev.sparkstart;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import spark.Spark;

public class Environment {

    private static final String APP_MODE_PROPERTY = "standalone.todo.manager.app";
    private static final String APP_MODE_ENV = "STANDALONE_TODO_MANAGER_APP";
    private static final String APP_ARGS_PROPERTY = "standalone.todo.manager.args";

    public static String getEnv(String urlPath) {
        return getBaseUri() + urlPath;
    }

    public static String getBaseUri() {

        // return environment if want to run externally
        //        if(true)
        //            return "https://somethingwhichhoststheapi.com";

        // switch rest assured logging on
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // if not running then start the spark
        if (Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        } else {
            // start it up
            Spark.port(4567);
            String[] args = configuredAppArgs();

            if (useSqliteMemoryApp()) {
                uk.co.compendiumdev.todolist.sqlite.application.Main.main(args);
            } else {
                uk.co.compendiumdev.todolist.application.Main.main(args);
            }

            // wait till running
            int maxtries = 10;
            while (!Port.inUse("localhost", 4567)) {
                maxtries--;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "http://localhost:4567";
        }

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }

    private static boolean useSqliteMemoryApp() {
        String appMode = System.getProperty(APP_MODE_PROPERTY);
        if (appMode == null || appMode.isBlank()) {
            appMode = System.getenv(APP_MODE_ENV);
        }

        return "sqlite-memory".equalsIgnoreCase(appMode) || "sqlite".equalsIgnoreCase(appMode);
    }

    private static String[] configuredAppArgs() {
        String configuredArgs = System.getProperty(APP_ARGS_PROPERTY, "");
        if (configuredArgs.isBlank()) {
            return new String[0];
        }

        return configuredArgs.split(",");
    }
}
