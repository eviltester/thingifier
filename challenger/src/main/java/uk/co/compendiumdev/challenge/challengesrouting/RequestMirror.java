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

    Thing entityDefn;

    // new RequestMirror().mirrorRequest(request, result);
    public String mirrorRequest(final Request request, final Response result) {

        final Thingifier mirrorThingifier = new Thingifier();
        mirrorThingifier.apiConfig().setResponsesToShowGuids(false);
        mirrorThingifier.apiConfig().setResponsesToShowIdsIfAvailable(false);

        entityDefn = mirrorThingifier.createThing("messageDetails", "messagesDetails");

        entityDefn.definition().addFields(
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
