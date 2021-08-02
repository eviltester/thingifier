package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.HttpApiResponseToSpark;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.SparkToHttpApiRequest;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import static spark.Spark.*;

public class MirrorRoutes {

    public void configure(final ThingifierApiDefn apiDefn) {

        // /mirror should be the GUI
        String endpoint ="/mirror/request";

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

            String body = mirrorRequest(request, result);
            if(result.status()==200){
                result.status(204);
                body = "";
            }

            return body;
        });

        head("/mirror/request/*", (request, result) -> {

            String body = mirrorRequest(request, result);
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

    // TODO: pull the code below into a RequestMirror class
    // new RequestMirror().mirrorRequest(request, result);
    private String mirrorRequest(final Request request, final Response result) {

        final Thingifier mirrorThingifier = new Thingifier();
        mirrorThingifier.apiConfig().setResponsesToShowGuids(false);
        mirrorThingifier.apiConfig().setResponsesToShowIdsIfAvailable(false);

        final Thing entityDefn = mirrorThingifier.createThing("messageDetails", "messagesDetails");

        entityDefn.definition().addFields(
                Field.is("details", FieldType.STRING));

        final HttpApiRequest myRequest = SparkToHttpApiRequest.convert(request);

        final JsonThing jsonThing = new JsonThing(mirrorThingifier.apiConfig().jsonOutput());


        ApiResponse response;

        // reject large requests
        if(rejectRequestTooLong(request, result)){

            response = ApiResponse.error(413, "Error: Request too large");
            final HttpApiResponse httpApiResponse = new HttpApiResponse(myRequest.getHeaders(), response,
                    jsonThing, mirrorThingifier.apiConfig());

            return HttpApiResponseToSpark.convert(httpApiResponse, result);
        }

        // handle input validation - mirror does not validate
//        response = httpApi.validateRequestSyntax(myRequest,
//                ThingifierHttpApi.HttpVerb.GET);


        // convert request into a string for message body- getRequestDetails
        String requestDetails = getRequestDetails(request);

        final AcceptHeaderParser parser = new AcceptHeaderParser(
                                                myRequest.getHeader("accept"));

        // handle text separately as the main api does not 'do' text
        // todo: add an api configuration to allow text as response type

        if(parser.hasAskedForTEXT()){
            result.header("Content-Type", "text/plain");
            result.status(200);
            return requestDetails;
        }

        // let main code handle formatting etc.
        ThingInstance fake = entityDefn.createInstance().
                setValue("details", requestDetails);
        response = ApiResponse.success().returnSingleInstance(fake);


        final HttpApiResponse httpApiResponse = new HttpApiResponse(myRequest.getHeaders(), response,
                jsonThing, mirrorThingifier.apiConfig());

        return HttpApiResponseToSpark.convert(httpApiResponse, result);
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
