package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import uk.co.compendiumdev.serverstart.Port;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;

public class Environment {

    private static MainImplementation app;

    /** could just use `RestAssured.baseURI = Environment.getBaseUri();` instead */
    public static String getEnv(String urlPath) {
        return getBaseUri() + urlPath;
    }

    // todo instead of setting up the Thingifier instantiate the Main with different version numbers
    // todo move these tests into the appropriate version package for the
    // standAloneTodoListManagerRestAuto project
    public static String getBaseUri() {

        // setup rest assured logging
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // if not running then start the server
        if (Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        } else {
            // start it up
            final Thingifier thingifier = new TodoManagerThingifier().get();
            thingifier.apiConfig().adminConfig().enableAdminDataClear();
            thingifier.apiConfig().adminConfig().enableAdminSearch();
            thingifier.apiConfig().setUrlToShowSingleInstancesAsPlural(true);
            thingifier.apiConfig().jsonOutput().setCompressRelationships(false);
            thingifier.apiConfig().jsonOutput().setShowPrimaryKeyInResponse(true);
            thingifier.apiConfig().jsonOutput().setConvertFieldsToDefinedTypes(false);

            app = new MainImplementation();
            app.registerModel("todoManager", thingifier);
            app.setDefaultsFromArgs(new String[0]);
            app.configurePortAndDefaultRoutes();
            app.setupBuiltInConfigurableRoutes();
            app.chooseThingifier();
            app.configureThingifierWithProfile();
            app.setupDefaultGui();
            app.startRestServer();
            waitTillRunningStatus(true);

            return "http://localhost:4567";
        }

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }

    public static void waitTillRunningStatus(final boolean running) {
        // wait till running
        int maxtries = 10;
        while (Port.inUse("localhost", 4567) != running) {
            maxtries--;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Interruption during running check " + e.getMessage());
            }
            if (maxtries <= 0) {
                return;
            }
        }
    }

    public static void stop() {
        if (app != null) {
            app.close();
            app = null;
        }
        waitTillRunningStatus(false);
    }
}
