package uk.co.compendiumdev.todolist.application;

import static spark.Spark.get;

import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.TodoListThingifier;

public class Main {

    public static void main(String[] args) {

        MainImplementation app = new MainImplementation();
        app.registerModel("simpleTodoList", new TodoListThingifier().get());

        // add any additional thingifier configurations here if more needed than model has defined
        app.setDefaultsFromArgs(args);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        app.chooseThingifier();
        // can set profile by adding more configs, or just
        // app.setProfileToUse(aProfile)
        app.configureThingifierWithProfile();

        app.setupDefaultGui();

        get(
                "/",
                (request, response) -> {
                    response.redirect("/gui/entities");
                    return "";
                });

        app.startRestServer();
        app.addBuiltInArgConfiguredHooks();
    }
}
