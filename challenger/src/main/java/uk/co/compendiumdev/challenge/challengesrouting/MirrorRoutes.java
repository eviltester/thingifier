package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.XML;
import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.BasicAuthHeaderParser;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import static spark.Spark.*;

public class MirrorRoutes {

    public void configure(final ThingifierApiDefn apiDefn) {

        // /mirror should be the GUI
        String endpoint ="/mirror/request";

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
            return mirrorRequest(request, result);
        });

        get("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        post("/mirror/request", (request, result) -> {
            return mirrorRequest(request, result);
        });

        post("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        delete("/mirror/request", (request, result) -> {
            return mirrorRequest(request, result);
        });

        delete("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        put("/mirror/request", (request, result) -> {
            return mirrorRequest(request, result);
        });

        put("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        patch("/mirror/request", (request, result) -> {
            return mirrorRequest(request, result);
        });

        patch("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        trace("/mirror/request", (request, result) -> {
            return mirrorRequest(request, result);
        });

        trace("/mirror/request/*", (request, result) -> {
            return mirrorRequest(request, result);
        });

        head("/mirror/request", (request, result) -> {

            // reject large requests
            if(rejectRequestTooLong(request, result))
                return asError(request.headers("Accept"), "Error: Request too large", result);

            final AcceptHeaderParser parser = new AcceptHeaderParser(request.headers("Accept"));

            if(parser.hasAPreferenceForXml()){
                result.header("Content-Type", "application/xml");
            }else {
                result.header("Content-Type", "application/json");
            }

            result.status(204);
            return "";
        });

        head("/mirror/request/*", (request, result) -> {

            // reject large requests
            if(rejectRequestTooLong(request, result))
                return asError(request.headers("Accept"), "Error: Request too large", result);

            final AcceptHeaderParser parser = new AcceptHeaderParser(request.headers("Accept"));

            if(parser.hasAPreferenceForXml()){
                result.header("Content-Type", "application/xml");
            }else {
                result.header("Content-Type", "application/json");
            }

            result.status(204);
            return "";
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

    private String mirrorRequest(final Request request, final Response result) {
        // reject large requests
        if(rejectRequestTooLong(request, result))
            return asError(request.headers("Accept"), "Error: Request too large", result);

        // convert request into a string - getRequestDetails
        String requestDetails = getRequestDetails(request);

        // return based on accept header format
        result.status(200);
        return asTextResponse(request.headers("Accept"), requestDetails, result);

    }

    private String asTextResponse(final String acceptHeader, final String requestDetails, final Response result) {
        return asField("messageDetails", acceptHeader, requestDetails, result);
    }

    private String asError(final String acceptHeader, final String errorMessage, final Response result) {
        return asField("error", acceptHeader, errorMessage, result);
    }

    private String asField(final String fieldName, final String acceptHeader, final String message, final Response result) {
        final AcceptHeaderParser parser = new AcceptHeaderParser(acceptHeader);

        if(parser.hasAPreferenceForXml()){
            result.header("Content-Type", "application/xml");
            return String.format("<%1$s>%2$s</%1$s>",fieldName, XML.escape(message));
        }

        if(parser.hasAPreferenceForJson()){
            result.header("Content-Type", "application/json");
            final JsonObject note = new JsonObject();
            note.addProperty(fieldName, message);
            return new Gson().toJson(note);
        }

        // default to text for mirroring
        result.header("Content-Type", "text/plain");
        return message;

    }

    private boolean rejectRequestTooLong(final Request request, final Response result) {
        if(request.contentLength()>24000){
            // randomly picked 24K
            result.status(413);
            return true;
        }
        return false;
    }

    private String getRequestDetails(final Request request) {

        StringBuilder output = new StringBuilder();

        output.append(String.format("%s %s",request.requestMethod(), request.url()));
        output.append("\n");

        output.append("\n");
        output.append("Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for(String queryParam : request.queryParams()){
            output.append(String.format("%s: %s",queryParam, request.queryParams(queryParam)));
            output.append("\n");
        }

        output.append("\n");
        output.append("IP");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        output.append(request.ip());
        output.append("\n");

        output.append("\n");
        output.append("Headers");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        for(String header : request.headers()){
            output.append(String.format("%s: %s",header, request.headers(header)));
            output.append("\n");
        }
        output.append("\n");
        output.append("Body");
        output.append("\n");
        output.append("====");
        output.append("\n");
        output.append(request.body());
        output.append("\n");
        return output.toString();
    }
}
