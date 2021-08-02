package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.HttpApiResponseToSpark;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.SparkToHttpApiRequest;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

public class RequestMirror {

    // TODO: pull the code below into a RequestMirror class
    // new RequestMirror().mirrorRequest(request, result);
    public String mirrorRequest(final Request request, final Response result) {

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
        SparkMessageLengthValidator lengthValidator = new SparkMessageLengthValidator();

        if(lengthValidator.rejectRequestTooLong(request, result)){
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
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
