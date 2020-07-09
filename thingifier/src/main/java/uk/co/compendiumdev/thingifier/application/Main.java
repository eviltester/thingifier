package uk.co.compendiumdev.thingifier.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUI;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;

/*

Backlog

- todo: create before/after api hooks to use Thingifier as an API framework as well as generator
- todo: split example models and main out of Thingifier and into a 'standalone api's app
- todo: api config collections for different 'releases' of the API - see standalone examples
- todo: allow switching between releases when it is running
- todo: styling of GUIs
- todo: export insomnia, swagger, raml, postman files,
- todo: have an auto updated 'create' and 'amend' date time stamp field - renamable in definition e.g. created-time, type CREATED-DATE, with a format
- todo: have an automatically added 'ping' url to see if service is running
- todo: allow configuring the default url mappings e.g. url("/gui).to(DEFAULT_API.systemGui)
- todo: documentation for routing urls should use GUID or ID based on config
- todo: HATEOS - based on config for GUID, Ids and Routings
- todo: allow switching on and off features at low and high level with well defined and consistent methods
    - exclusively use compressed relationships
        - output use compressed relationship
        - input enforce compressed relationship(true)
    - exclusively use IDs
        - show guids in responses(false)
        - show relationships using ids where available(true)
        - show ids in urls where available(true)
        - allow guids in urls(false)
        - allow ids in urls(true)
    - exclusively use GUIDs
        - show guids in responses(true)
        - show relationships using ids where available(false)
        - show ids in urls where available(false)
        - allow guids in urls(true)
        - allow ids in urls(false)
    - exclusively use XML
        - sub config...
        - can accept XML in requests (true)
        - can output XML in responses (true)
        - can accept JSON in requests (false)
        - can output JSON in responses (false)
        - reports accepting XML in requests (true)
        - reports outputing XML in responses (true)
        - output default(XML)
        - input default(XML)
    - exclusively use JSON
        - sub config...
        - can accept XML in requests (false)
        - can output XML in responses (false)
        - can accept JSON in requests (true)
        - can output JSON in responses (true)
        - reports accepting JSON in requests (true)
        - reports outputing JSON in responses (true)
        - reports accepting XML in requests (false)
        - reports outputing XML in responses (false)
        - output default(JSON)
        - input default(JSON)
- todo: allow nested objects, not just relationships e.g. GET shows related objects as top level fields, or full objects
- todo: allow creating and amending with nested objects
- todo: allow one way relationships as single fields e.g. project: {guid}
- todo: allow grouped fields, e.g. bought{from:"", when,""} - shown as nested in the json views
- todo: support different authentication methods
- todo: allow user accounts


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
