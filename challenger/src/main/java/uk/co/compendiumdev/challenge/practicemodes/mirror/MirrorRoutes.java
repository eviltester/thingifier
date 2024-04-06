package uk.co.compendiumdev.challenge.practicemodes.mirror;

import spark.Route;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.AdhocDocumentedSparkRouteConfigurer;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;


import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class MirrorRoutes {

    public void configure(final ThingifierApiDocumentationDefn apiDefn) {

        // /mirror should be the GUI
        String endpoint ="/mirror/request";
        String rawEndPoint ="/mirror/raw";

        RequestMirror requestMirror = new RequestMirror();

        // redirect a GET to "/fromPath" to "/toPath"
        redirect.get("/mirror", "/practice-modes/mirror");

        List<String>verbEndpoints = new ArrayList<>();
        verbEndpoints.add(endpoint);
        verbEndpoints.add(endpoint+"/*");
        verbEndpoints.add(rawEndPoint);
        verbEndpoints.add(rawEndPoint+"/*");

        AdhocDocumentedSparkRouteConfigurer routeCreatorAndDocumentor = new AdhocDocumentedSparkRouteConfigurer(apiDefn);

        for (String anEndpoint : verbEndpoints) {
            Route routeHandler = (request, result) -> {
                result.status(204);
                result.header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE");
                return "";
            };
            if(anEndpoint.endsWith("/*")){
                // add to routing but not to the api documentation
                SimpleSparkRouteCreator.addHandler(anEndpoint, "options", routeHandler);
            }else {
                routeCreatorAndDocumentor.
                        add(anEndpoint, RoutingVerb.OPTIONS, 204,
                                "Options for mirror endpoint",
                                routeHandler
                        );
            }
        }

        Route mirroredRoute = (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        };

        Route rawTextMirroredRoute = (request, result) -> {
            return requestMirror.mirrorRequestAsText(request, result);
        };

        RoutingVerb[] verbs200status = {RoutingVerb.GET, RoutingVerb.POST, RoutingVerb.PUT,
                                        RoutingVerb.DELETE, RoutingVerb.PATCH, RoutingVerb.TRACE};

        for (String anEndpoint : verbEndpoints) {
            for(RoutingVerb routing : verbs200status) {
                if(anEndpoint.endsWith("/*")){
                    // add to routing but not to the api documentation
                    SimpleSparkRouteCreator.addHandler(anEndpoint, routing.name(), mirroredRoute);
                }else {
                    if (anEndpoint.startsWith(endpoint)) {
                        routeCreatorAndDocumentor.add(anEndpoint, routing, 200,
                                "Mirror a " + routing.name().toUpperCase() + " Request", mirroredRoute);
                    }
                    if (anEndpoint.startsWith(rawEndPoint)) {
                        routeCreatorAndDocumentor.add(anEndpoint, routing, 200,
                                "Raw Text Mirror of a " + routing.name().toUpperCase() + " Request", rawTextMirroredRoute);
                    }
                }
            }
        }

        for (String anEndpoint : verbEndpoints) {
            Route routeHAndler = (request, result) -> {
                String body = requestMirror.mirrorRequest(request, result);
                return "";
            };
            if(anEndpoint.endsWith("/*")){
                // add to routing but not to the api documentation
                SimpleSparkRouteCreator.addHandler(anEndpoint, "head", routeHAndler);
            }else {
                routeCreatorAndDocumentor.
                        add(anEndpoint, RoutingVerb.HEAD, 204,
                                "Headers for mirror endpoint",
                                routeHAndler
                        );
            }
        }

        get("/practice-modes/mirror/swagger", (request, response) ->{
            response.status(200);
            response.header("content-type", "application/json");
            response.header("x-robots-tag", "noindex");
            response.header("Content-Type", "application/octet-stream");
            response.header("Content-Disposition",
                    "attachment; filename=\"mirrormodeswagger.json\"");
            return new Swaggerizer(apiDefn).asJson();
        });

   }
}
