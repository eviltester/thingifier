package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;


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

        // add the documentation
        RoutingVerb[] verbs = { RoutingVerb.GET, RoutingVerb.POST, RoutingVerb.PUT,
                                RoutingVerb.DELETE, RoutingVerb.PATCH, RoutingVerb.TRACE,
                                RoutingVerb.OPTIONS, RoutingVerb.HEAD};

        String [] documentation = {"Mirror a GET Request", "Mirror a POST Request", "Mirror a PUT Request",
                                    "Mirror a DELETE Request", "Mirror a PATCH Request", "Mirror a TRACE Request",
                                    "Options for mirror endpoint", "Headers for mirror endpoint"};

        int [] statusCodes = {  200, 200, 200,
                                200, 200, 200,
                                204, 200};

        for(int index=0; index< verbs.length; index++){
            apiDefn.addRouteToDocumentation(
                    new RoutingDefinition(
                            verbs[index],
                            endpoint,
                            RoutingStatus.returnedFromCall(),
                            null).addDocumentation(documentation[index]).
                            addPossibleStatuses(statusCodes[index]));
        }

    }


}
