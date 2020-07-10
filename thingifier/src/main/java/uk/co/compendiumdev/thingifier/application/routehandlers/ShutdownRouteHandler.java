package uk.co.compendiumdev.thingifier.application.routehandlers;

import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;

public class ShutdownRouteHandler {

    List<RoutingDefinition> routes;

    public ShutdownRouteHandler(){
        routes = new ArrayList();
    }

    public List<RoutingDefinition> getRoutes(){
        return routes;
    }

    public ShutdownRouteHandler configureRoutes() {

        get("/shutdown", (request, result) -> {
            System.exit(0);
            return "";
        });

        routes.add(new RoutingDefinition(
                            RoutingVerb.GET,
                        "/shutdown",
                            RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Shutdown the API server"));

        return this;
    }
}
