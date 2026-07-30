package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import uk.co.compendiumdev.thingifier.adapter.httpserver.MainImplementation;
import uk.co.compendiumdev.thingifier.application.examples.ConfiguredWriteMethodsThingifier;

public class Main {

    public static void main(String[] args) {
        MainImplementation app = new MainImplementation();
        app.registerModel("configuredWriteMethods", new ConfiguredWriteMethodsThingifier().get());

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
