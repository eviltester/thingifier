package uk.co.compendiumdev.todolist.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUI;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;

import static spark.Spark.get;
import static spark.Spark.staticFiles;

public class Main {
    static boolean hasHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        return (processBuilder.environment().get("PORT") != null);
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (hasHerokuAssignedPort()) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return -1; //return default port if heroku-port isn't set (i.e. on localhost)
    }

    public static void main(String[] args) {


        Integer proxyport = 4567;    // default for spark

        // added to support heroku as per https://sparktutorials.github.io/2015/08/24/spark-heroku.html
        // environment can override config for port
        if (hasHerokuAssignedPort()) {
            proxyport = getHerokuAssignedPort();
        }

        int version = 3;
        System.out.println("Number of app versions available (e.g. -version=2) are: " + version);


        for (String arg : args) {
            System.out.println("Args: " + arg);

            if (arg.startsWith("-port")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    proxyport = Integer.parseInt(details[1].trim());
                    System.out.println("Will configure web server to use port " + proxyport);
                }
            }

            if (arg.startsWith("-version")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    version = Integer.parseInt(details[1].trim());
                }
            }

        }


        Spark.port(proxyport);
        staticFiles.location("/public");

        get("/shutdown", (request, result) -> {
            System.exit(0);
            return "";
        });

        Thingifier thingifier;

        thingifier = new TodoManagerThingifier().get();

        // can have different -version params which configure the TodoManagerThingifier in different ways
        // e.g. v1 non compressed relationships with guids
        // e.g. v2 compressed relationships with guids
        // e.g. v3 compressed relationships with ids
        // default the app to v3 to make it easier for people
        switch(version){
            case 1:
                thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(false);
                thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(false);
                thingifier.apiConfig().showSingleInstancesAsPlural(true);
                thingifier.apiConfig().allowShowGuidsInResponses(true);
                thingifier.apiConfig().jsonOutput().compressRelationships(false);
                thingifier.apiConfig().jsonOutput().relationshipsUsesIdsIfAvailable(false);
                thingifier.apiConfig().jsonOutput().convertFieldsToDefinedTypes(false);
                thingifier.apiConfig().shouldEnforceDeclaredTypesInInput(false);
                break;
            case 2:
                thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(false);
                thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(false);
                thingifier.apiConfig().showSingleInstancesAsPlural(true);
                thingifier.apiConfig().allowShowGuidsInResponses(true);
                thingifier.apiConfig().jsonOutput().compressRelationships(true);
                thingifier.apiConfig().jsonOutput().relationshipsUsesIdsIfAvailable(false);
                thingifier.apiConfig().jsonOutput().convertFieldsToDefinedTypes(false);
                thingifier.apiConfig().shouldEnforceDeclaredTypesInInput(false);
                break;
            case 3:
                thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(true);
                thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(true);
                thingifier.apiConfig().showSingleInstancesAsPlural(true);
                thingifier.apiConfig().allowShowGuidsInResponses(false);
                thingifier.apiConfig().jsonOutput().compressRelationships(true);
                thingifier.apiConfig().jsonOutput().relationshipsUsesIdsIfAvailable(true);
                thingifier.apiConfig().jsonOutput().convertFieldsToDefinedTypes(false);
                thingifier.apiConfig().shouldEnforceDeclaredTypesInInput(true);
                break;
        }
        new DefaultGUI(thingifier).setupDefaultGUI();

        ThingifierRestServer restServer;

        restServer = new ThingifierRestServer(args, "", thingifier);

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

    }
}
