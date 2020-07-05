package uk.co.compendiumdev.thingifier.application;

import spark.Service;
import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.reporting.RestApiDocumentationGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.get;

/*

Backlog

- todo: have an auto updated 'create' and 'amend' date time stamp field - renamable in definition e.g. created-time, type CREATED-DATE, with a format
- todo: have an automatically added 'ping' url to see if service is running
- todo: create a 'view' gui that shows all the items e.g. /system/gui
- todo: allow configuring the default url mappings e.g. url("/gui).to(DEFAULT_API.systemGui)
- todo: allow switing on and off features
- todo: allow nested objects, not just relationships e.g. GET shows related objects as top level fields, or full objects
- todo: allow creating and amending with nested objects
- todo: allow grouped fields, e.g. bought{from:"", when,""} - shown as nested in the json views

 */

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

        List<String> validModelNames = new ArrayList();
        validModelNames.add("todoListManager");
        validModelNames.add("simpleTodoList");

        String modelName=validModelNames.get(0);

        System.out.println("Valid Model Names -model=");
        for(String aModelName : validModelNames){
            System.out.println(aModelName);
        }

        for (String arg : args) {
            System.out.println("Args: " + arg);

            if (arg.startsWith("-port")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    proxyport = Integer.parseInt(details[1].trim());
                    System.out.println("Will configure web server to use port " + proxyport);
                }
            }

            if (arg.startsWith("-model")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    String argModelName = details[1].trim();
                    if(validModelNames.contains(argModelName)){
                        modelName = argModelName;
                        System.out.println("Will use model named " + modelName);
                    }else{
                        System.out.println(
                                String.format("Invalid model name %s, using %s",
                                        argModelName, modelName));
                    }
                }
            }
        }


        Spark.port(proxyport);

        // todo : add shutdown behind an admin authentication with basic auth and a custom secret code header
        // todo : add some other admin endpoints e.g. show version details of the app etc.
        // TODO: add a shutdown verb as configurable through arguments e.g. -shutdownable=false
        get("/shutdown", (request, result) -> {
            System.exit(0);
            return "";
        });

        Thingifier thingifier;

        switch (modelName){
            case "simpleTodoList":
                thingifier =  new TodoListThingifier().get();
            case "todoListManager":
            default:
                thingifier = new TodoManagerThingifier().get();
        }

        new DefaultGUI(thingifier).setupDefaultGUI();

        ThingifierRestServer restServer;

        switch (modelName){
            case "simpleTodoList":
                restServer = new ThingifierRestServer(args, "", thingifier);
            case "todoListManager":
            default:
                restServer = new ThingifierRestServer(args, "", thingifier);
        }



        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

    }


}
