package uk.co.compendiumdev.thingifier.adapter.httpserver.routehandlers;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public class ShutdownRouteHandler {

    List<RoutingDefinition> routes;
    private final AutoCloseable closeable;

    public ShutdownRouteHandler() {
        this(null);
    }

    public ShutdownRouteHandler(final AutoCloseable closeable) {
        routes = new ArrayList();
        this.closeable = closeable;
    }

    public List<RoutingDefinition> getRoutes() {
        return routes;
    }

    public ShutdownRouteHandler configureRoutes() {

        get(
                "/shutdown",
                (request, result) -> {
                    if (closeable != null) {
                        closeable.close();
                    }
                    System.exit(0);
                    return "";
                });

        routes.add(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                "/shutdown",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("Shutdown the API server")
                        .addPossibleStatuses(200));

        return this;
    }
}
