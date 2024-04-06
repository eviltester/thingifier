package uk.co.compendiumdev.challenge.practicemodes.mirror;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.spark.SparkMessageLengthValidator;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.application.routehandlers.SparkApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class RequestMirror {

    EntityDefinition entityDefn;

    // new RequestMirror().mirrorRequest(request, result);
    public String mirrorRequest(final Request request, final Response result) {

        final Thingifier mirrorThingifier = new Thingifier();

        entityDefn = mirrorThingifier.defineThing("messageDetails", "messagesDetails");

        entityDefn.addFields(
                Field.is("details", FieldType.STRING));

        // reject large requests
        SparkMessageLengthValidator lengthValidator = new SparkMessageLengthValidator();

        if(lengthValidator.rejectRequestTooLong(request, result)){
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
        }

        String returnValue =  new SparkApiRequestResponseHandler(request, result, mirrorThingifier).
                usingHandler(
                        new MirrorHttpApiRequestHandler(this.entityDefn)
                ).validateRequestSyntax(false).handle();

        final AcceptHeaderParser parser = new AcceptHeaderParser(request.headers("accept"));

        // handle text separately as the main api does not 'do' text
        if(parser.hasAskedForTEXT()){
            result.header("Content-Type", "text/plain");
        }

        return returnValue;
    }

    public String mirrorRequestAsText(final Request request, final Response result) {

        // The raw unfiltered request as text

        final Thingifier mirrorThingifier = new Thingifier();

        // reject large requests
        SparkMessageLengthValidator lengthValidator = new SparkMessageLengthValidator();

        if(lengthValidator.rejectRequestTooLong(request, result)){
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
        }

        String returnValue =  new SparkApiRequestResponseHandler(request, result, mirrorThingifier).
                usingHandler(
                        new MirrorHttpApiTextRequestHandler()
                ).validateRequestSyntax(false).handle();

        result.header("Content-Type", "text/plain");

        return returnValue;
    }

}
