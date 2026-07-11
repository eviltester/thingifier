package uk.co.compendiumdev.todolist.sqlite.application;

import static spark.Spark.get;

import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class Main {

    public static void main(String[] args) {

        MainImplementation app = new MainImplementation();
        app.registerModel(
                "todoManager",
                new TodoManagerThingifier().get(SqliteThingStoreProvider.inMemory()));

        app.setDefaultsFromArgs(args);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        app.chooseThingifier();
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
