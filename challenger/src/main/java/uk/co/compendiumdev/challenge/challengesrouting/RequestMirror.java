package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.routehandlers.SparkApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class RequestMirror {

    EntityDefinition entityDefn;

    // new RequestMirror().mirrorRequest(request, result);
    public String mirrorRequest(final Request request, final Response result) {

        final Thingifier mirrorThingifier = new Thingifier();
        mirrorThingifier.apiConfig().setResponsesToShowGuids(false);
        mirrorThingifier.apiConfig().setResponsesToShowIdsIfAvailable(false);

        entityDefn = mirrorThingifier.defineThing("messageDetails", "messagesDetails");

        entityDefn.addFields(
                Field.is("details", FieldType.STRING));

        // reject large requests
        SparkMessageLengthValidator lengthValidator = new SparkMessageLengthValidator();

        if(lengthValidator.rejectRequestTooLong(request, result)){
            return lengthValidator.messageTooLongErrorResponse(
                    mirrorThingifier.apiConfig(), request, result);
        }

        return new SparkApiRequestResponseHandler(request, result, mirrorThingifier).
                usingHandler(
                        new MirrorHttpApiRequestHandler(this.entityDefn)
                ).validateRequestSyntax(false).handle();

    }

}
