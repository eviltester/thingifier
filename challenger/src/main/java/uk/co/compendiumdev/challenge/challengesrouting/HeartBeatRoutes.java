package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.httpserver.AdhocDocumentedHttpRouteConfigurer;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

public class HeartBeatRoutes {

    public void configure(final ThingifierApiDocumentationDefn apiDefn) {

        String endpoint = "/heartbeat";

        final AdhocDocumentedHttpRouteConfigurer routeConfig =
                new AdhocDocumentedHttpRouteConfigurer(apiDefn);

        routeConfig.add(endpoint, RoutingVerb.GET, 204, "Is the server running? YES 204");

        routeConfig.add(endpoint, RoutingVerb.HEAD, 204, "Headers for heartbeat endpoint");

        routeConfig.add(
                endpoint,
                RoutingVerb.OPTIONS,
                204,
                "Options for heartbeat endpoint",
                (request, result) -> {
                    result.status(204);
                    result.header("Allow", "GET, HEAD, OPTIONS");
                    return "";
                });

        // undocumented handlers
        SimpleHttpRouteCreator SimpleHttpRouteCreator = new SimpleHttpRouteCreator(endpoint);
        SimpleHttpRouteCreator.status(405, true, List.of("post", "delete", "put"));
        SimpleHttpRouteCreator.status(500, true, List.of("patch"));
        SimpleHttpRouteCreator.status(501, true, List.of("trace"));
    }
}
