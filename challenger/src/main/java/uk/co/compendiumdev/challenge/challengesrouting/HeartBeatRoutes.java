package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.AdhocDocumentedSparkRouteConfig;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import static spark.Spark.options;

public class HeartBeatRoutes {

    public void configure(final ThingifierApiDefn apiDefn) {

        String endpoint ="/heartbeat";

        final AdhocDocumentedSparkRouteConfig sparkRouteConfig =
                new AdhocDocumentedSparkRouteConfig(apiDefn);

        sparkRouteConfig.add(endpoint,
                            RoutingVerb.GET, 204,
                "Is the server running? YES 204");

        sparkRouteConfig.add(endpoint,
                RoutingVerb.HEAD, 204,
                "Headers for heartbeat endpoint");

        sparkRouteConfig.add(endpoint,
                RoutingVerb.OPTIONS, 204,
                "Options for heartbeat endpoint",
                (request, result) -> {
                    result.status(204);
                    result.header("Allow", "GET, HEAD, OPTIONS");
                    return "";
                }
                );

        // undocumented handlers
        new SimpleRouteConfig(endpoint).
                status(405,  "post", "delete", "put").
                status(500,  "patch").
                status(501, "trace");

    }
}
