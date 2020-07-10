package uk.co.compendiumdev.thingifier.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.application.routehandlers.ShutdownRouteHandler;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUI;

import java.util.ArrayList;
import java.util.List;


/*

Backlog

- todo: styling of GUIs
- todo: allow shutdown to be prevented at command line e.g. -noshutdown
- todo: release as basic test api with multiple versions
- todo: generate a guid which we render at the command line on startup and -admin=base64usernamepassword default to admin:password to host admin urls behind
- todo: add an option allow admin urls to enable, disable admin urls
- todo: support queries on the entities e.g. /todos?field=exactmatch
- todo: support tearing down and reset every 10 mins for a heroku deploy
- todo: create before/after api hooks to use Thingifier as an API framework as well as generator
- todo: split example models and main out of Thingifier and into a 'standalone api's app
- todo: api config collections for different 'releases' of the API - see standalone examples
- todo: allow switching between releases when it is running
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

        // prevent shutdown verb as configurable through arguments e.g. -noshutdown
        Boolean allowShutdown = true;

        // clear data every 10 minutes configuragle through arguments e.g. -autocleardown
        // -autocleardown=15
        Boolean clearDataPeriodically = false;
        int clearDownMinutes=10;

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

            if (arg.startsWith("-noshutdown")) {
                allowShutdown = false;
            }

            if (arg.startsWith("-autocleardown")) {
                clearDataPeriodically = true;
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    String minutes = details[1].trim();
                    try{
                        clearDownMinutes = Integer.valueOf(minutes);
                    }catch(Exception e){
                        System.out.println("Invalid minutes " + minutes + " " +e.getMessage());
                    }
                    System.out.println(String.format("Will clear down every %d minutes", clearDownMinutes));
                }
            }
        }


        Spark.port(proxyport);
        Spark.staticFileLocation("/public");


        List<RoutingDefinition> additionalRoutes = new ArrayList<>();


        // todo : add shutdown behind an admin authentication with basic auth and a custom secret code header
        // todo : add some other admin endpoints e.g. show version details of the app etc.


        if(allowShutdown) {
            additionalRoutes.addAll(
                    new ShutdownRouteHandler().
                            configureRoutes().
                            getRoutes());
        }

        Thingifier thingifier;

        switch (modelName){
            case "simpleTodoList":
                thingifier =  new TodoListThingifier().get();
                break;
            case "todoListManager":
            default:
                thingifier = new TodoManagerThingifier().get();
        }

        thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(true);
        thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(true);
        thingifier.apiConfig().showSingleInstancesAsPlural(true);
        thingifier.apiConfig().allowShowGuidsInResponses(false);

        additionalRoutes.addAll(new DefaultGUI(thingifier).
                                configureRoutes().
                                getRoutes());





        ThingifierRestServer restServer;

        switch (modelName){
            case "simpleTodoList":
                restServer = new ThingifierRestServer(args, "",
                                                thingifier,
                                                additionalRoutes);
            case "todoListManager":
            default:
                restServer = new ThingifierRestServer(
                                        args, "",
                                        thingifier,
                                        additionalRoutes);
        }

        if(clearDataPeriodically) {
            restServer.registerPreRequestHook(
                    new ClearDataPreRequestHook(clearDownMinutes, thingifier));
        }

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

    }


}
