package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Route;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.AdhocDocumentedSparkRouteConfig;


import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class MirrorRoutes {

    public void configure(final ThingifierApiDefn apiDefn) {

        // /mirror should be the GUI
        String endpoint ="/mirror/request";
        RequestMirror requestMirror = new RequestMirror();

        // redirect a GET to "/fromPath" to "/toPath"
        redirect.get("/mirror", "/mirror.html");

        List<String>verbEndpoints = new ArrayList<>();
        verbEndpoints.add(endpoint);
        verbEndpoints.add(endpoint+"/*");

        AdhocDocumentedSparkRouteConfig routeDefn = new AdhocDocumentedSparkRouteConfig(apiDefn);

        for (String anEndpoint : verbEndpoints) {
            routeDefn.
                add(anEndpoint, RoutingVerb.OPTIONS, 204,
                        "Options for mirror endpoint",
                    (request, result) -> {
                        result.status(204);
                        result.header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE");
                        return "";
                    }
            );
        }

        Route mirroredRoute = (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        };

        RoutingVerb[] verbs200status = {RoutingVerb.GET, RoutingVerb.POST, RoutingVerb.PUT,
                                        RoutingVerb.DELETE, RoutingVerb.PATCH, RoutingVerb.TRACE};

        for (String anEndpoint : verbEndpoints) {
            for(RoutingVerb routing : verbs200status) {
                routeDefn.add(anEndpoint, routing, 200,
                        "Mirror a " + routing.name().toUpperCase() + " Request", mirroredRoute);
            }
        }

        for (String anEndpoint : verbEndpoints) {
            routeDefn.
                add(anEndpoint, RoutingVerb.HEAD, 204,
                        "Headers for mirror endpoint",
                        (request, result) -> {
                            String body = requestMirror.mirrorRequest(request, result);
                            return "";
                        }
                );
        }

   }
}
