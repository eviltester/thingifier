package uk.co.compendiumdev.todolist.application;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;

public class Main {

    public static void main(String[] args) {


        MainImplementation app = new MainImplementation();
        app.registerModel("simpleTodoList", new TodoListThingifier().get());

        app.setDefaultsFromArgs(args);

        int version = 3;
        System.out.println("Number of app versions available (e.g. -version=2) are: " + version);
        for (String arg : args) {
            if (arg.startsWith("-version")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    version = Integer.parseInt(details[1].trim());
                }
            }
        }
        System.out.println("Will configure app as release version " + version);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        Thingifier thingifier = app.chooseThingifier();

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
                break;
            case 2:
                thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(false);
                thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(false);
                thingifier.apiConfig().showSingleInstancesAsPlural(true);
                thingifier.apiConfig().allowShowGuidsInResponses(true);
                break;
            case 3:
                thingifier.apiConfig().allowShowIdsInUrlsIfAvailable(true);
                thingifier.apiConfig().allowShowIdsInResponsesIfAvailable(true);
                thingifier.apiConfig().showSingleInstancesAsPlural(true);
                thingifier.apiConfig().allowShowGuidsInResponses(false);
                break;
        }

        app.setupDefaultGui();
        app.startRestServer();
        app.addBuiltInArgConfiguredHooks();

    }
}
