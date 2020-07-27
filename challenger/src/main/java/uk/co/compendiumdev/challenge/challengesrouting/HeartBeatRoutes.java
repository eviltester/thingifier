package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.List;

import static spark.Spark.options;

public class HeartBeatRoutes {

    public void configure(final List<RoutingDefinition> routes) {

        String endpoint ="/heartbeat";

        options(endpoint, (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        new SimpleRouteConfig(endpoint).
                status(204, "get", "head").
                status(405,  "post", "delete", "put").
                status(500,  "patch").
                status(501, "trace");

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Is the server running? YES == 204"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for heartbeat endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for heartbeat endpoint"));
    }
}
