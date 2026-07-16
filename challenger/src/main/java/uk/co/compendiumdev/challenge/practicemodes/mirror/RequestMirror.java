package uk.co.compendiumdev.challenge.practicemodes.mirror;

import uk.co.compendiumdev.challenge.httpserver.HttpMessageLengthValidator;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.routehandlers.HttpApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class RequestMirror {

    EntityDefinition entityDefn;

    // new RequestMirror().mirrorRequest(request, result);
    public String mirrorRequest(final HttpServerRequest request, final HttpServerResponse result) {

        final Thingifier mirrorThingifier = new Thingifier();

        entityDefn = mirrorThingifier.defineThing("messageDetails", "messagesDetails");

        entityDefn.addFields(Field.is("details", FieldType.STRING));

        // reject large requests
        HttpMessageLengthValidator lengthValidator = new HttpMessageLengthValidator();

        if (lengthValidator.rejectRequestTooLong(request, result)) {
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
        }

        String returnValue =
                new HttpApiRequestResponseHandler(request, result, mirrorThingifier)
                        .usingHandler(new MirrorHttpApiRequestHandler(this.entityDefn))
                        .validateRequestSyntax(false)
                        .handle();

        final AcceptHeaderParser parser = new AcceptHeaderParser(request.header("accept"));

        // handle text separately as the main api does not 'do' text
        if (parser.hasAskedForTEXT()) {
            result.header("Content-Type", "text/plain");
        }

        return returnValue;
    }

    public String mirrorRequestAsText(
            final HttpServerRequest request, final HttpServerResponse result) {

        // The raw unfiltered request as text

        final Thingifier mirrorThingifier = new Thingifier();

        // reject large requests
        HttpMessageLengthValidator lengthValidator = new HttpMessageLengthValidator();

        if (lengthValidator.rejectRequestTooLong(request, result)) {
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
        }

        String returnValue =
                new HttpApiRequestResponseHandler(request, result, mirrorThingifier)
                        .usingHandler(new MirrorHttpApiTextRequestHandler())
                        .validateRequestSyntax(false)
                        .handle();

        result.header("Content-Type", "text/plain");

        return returnValue;
    }
}
