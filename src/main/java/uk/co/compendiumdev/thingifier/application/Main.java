package uk.co.compendiumdev.thingifier.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.get;

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

        ThingifierRestServer restServer;

        switch (modelName){
            case "simpleTodoList":
                restServer = new ThingifierRestServer(args, "", new TodoListThingifier().get());
            case "todoListManager":
            default:
                restServer = new ThingifierRestServer(args, "", new TodoManagerThingifier().get());
        }

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

    }
}
