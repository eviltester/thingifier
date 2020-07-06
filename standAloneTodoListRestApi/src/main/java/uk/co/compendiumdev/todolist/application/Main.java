package uk.co.compendiumdev.todolist.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.DefaultGUI;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;

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

        for (String arg : args) {
            System.out.println("Args: " + arg);

            if (arg.startsWith("-port")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    proxyport = Integer.parseInt(details[1].trim());
                    System.out.println("Will configure web server to use port " + proxyport);
                }
            }

        }


        Spark.port(proxyport);

        get("/shutdown", (request, result) -> {
            System.exit(0);
            return "";
        });

        Thingifier thingifier;

        thingifier = new TodoListThingifier().get();

        new DefaultGUI(thingifier).setupDefaultGUI();

        ThingifierRestServer restServer;

        restServer = new ThingifierRestServer(args, "", thingifier);

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

    }
}
