package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.AdhocDocumentedSparkRouteConfigurer;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;
import java.util.List;


public class HeartBeatRoutes {

    public void configure(final ThingifierApiDocumentationDefn apiDefn) {

        String endpoint ="/heartbeat";

        final AdhocDocumentedSparkRouteConfigurer sparkRouteConfig =
                new AdhocDocumentedSparkRouteConfigurer(apiDefn);

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
        SimpleRouteConfig simpleRouteConfig = new SimpleRouteConfig(endpoint);
        simpleRouteConfig.status(405, List.of("post", "delete", "put"));
        simpleRouteConfig.status(500, List.of("patch"));
        simpleRouteConfig.status(501, List.of("trace"));

    }
}
