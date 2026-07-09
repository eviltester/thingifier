package uk.co.compendiumdev.todolist.application;

import static spark.Spark.get;
import static spark.route.HttpMethod.get;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;

public class Main {

    public static void main(String[] args) {

        MainImplementation app = new MainImplementation();
        app.registerModel("todoManager", new TodoManagerThingifier().get());

        app.setDefaultsFromArgs(args);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        Thingifier thingifier = app.chooseThingifier();
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
