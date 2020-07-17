package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;

import static spark.Spark.get;


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

        MainImplementation app = new MainImplementation();

        app.registerModel("todoListManager", new TodoManagerThingifier().get());
        app.registerModel("simpleTodoList", new TodoListThingifier().get(), true);

        app.setDefaultsFromArgs(args);

        // could process args independently here
        // or override args
        // e.g. set port independently to a different default
        if(hasHerokuAssignedPort()) {
            app.setPort(getHerokuAssignedPort());
        }
        //app.setStaticFileLocation("/public");
        //app.setAllowShutdown(false);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        // todo : add shutdown behind an admin authentication with basic auth and a custom secret code header
        // todo : add some other admin endpoints e.g. show version details of the app etc.

        // allows thingifier to be used in additional custom route configuration
        // or apiConfig()
        // setup the thingifier
        Thingifier thingifier = app.chooseThingifier();

        // can set profile by adding more configs, or just
        // app.setProfileToUse(aProfile)
        app.configureThingifierWithProfile();

        // can configure additional routes and processing
//        app.addAdditionalRoutes(new DefaultGUI(thingifier).
//                                    configureRoutes().
//                                getRoutes());
        app.setupDefaultGui();


        // returning the restServer supports adding more 'hooks'
        ThingifierRestServer restServer = app.startRestServer();

        app.addBuiltInArgConfiguredHooks();


    }


}
