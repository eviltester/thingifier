package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Request;
import spark.Response;
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


        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.GET,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Mirror a GET Request").
                addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                        RoutingVerb.POST,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Mirror a POST Request").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                        RoutingVerb.PUT,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Mirror a PUT Request").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                        RoutingVerb.DELETE,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Mirror a DELETE Request").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                        RoutingVerb.PATCH,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Mirror a PATCH Request").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                        RoutingVerb.TRACE,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Mirror a TRACE Request").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.OPTIONS,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for mirror endpoint")
                        .addPossibleStatuses(204));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.HEAD,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for mirror endpoint")
                    .addPossibleStatuses(200));
    }


}
