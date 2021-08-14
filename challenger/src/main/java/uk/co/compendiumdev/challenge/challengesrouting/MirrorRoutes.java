package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;


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

        options(endpoint, (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE");
            return "";
        });

        options(endpoint +"/*", (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE");
            return "";
        });

        get("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        get("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        post("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        post("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        delete("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        delete("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        put("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        put("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        patch("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        patch("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        trace("/mirror/request", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        trace("/mirror/request/*", (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        });

        head("/mirror/request", (request, result) -> {

            String body = requestMirror.mirrorRequest(request, result);
            if(result.status()==200){
                result.status(204);
                body = "";
            }

            return body;
        });

        head("/mirror/request/*", (request, result) -> {

            String body = requestMirror.mirrorRequest(request, result);
            if(result.status()==200){
                result.status(204);
                body = "";
            }

            return body;
        });

        class Routing{
            public final RoutingVerb verb;
            public final String description;
            public final int statusCode;

            Routing(RoutingVerb verb, String description, int statusCode){
                this.verb = verb;
                this.description = description;
                this.statusCode = statusCode;
            }
        }

        List<Routing> routings = new ArrayList<>();
        routings.add(new Routing(RoutingVerb.GET, "Mirror a GET Request", 200));
        routings.add(new Routing(RoutingVerb.POST, "Mirror a POST Request", 200));
        routings.add(new Routing(RoutingVerb.PUT, "Mirror a PUT Request", 200));
        routings.add(new Routing(RoutingVerb.DELETE, "Mirror a DELETE Request", 200));
        routings.add(new Routing(RoutingVerb.PATCH, "Mirror a PATCH Request", 200));
        routings.add(new Routing(RoutingVerb.TRACE, "Mirror a TRACE Request", 200));
        routings.add(new Routing(RoutingVerb.OPTIONS, "Options for mirror endpoint", 204));
        routings.add(new Routing(RoutingVerb.HEAD, "Headers for mirror endpoint", 200));

        for(Routing routing : routings){
            apiDefn.addRouteToDocumentation(
                    new RoutingDefinition(
                            routing.verb,
                            endpoint,
                            RoutingStatus.returnedFromCall(),
                            null).addDocumentation(routing.description).
                            addPossibleStatuses(routing.statusCode));
        }
    }
}
