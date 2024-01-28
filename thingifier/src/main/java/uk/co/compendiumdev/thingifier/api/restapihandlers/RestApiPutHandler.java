package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.Map;

public class RestApiPutHandler {
    final Thingifier thingifier;

    public RestApiPutHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final BodyParser args, final Map<String, String>requestHeaders) {

        String instanceDatabaseName = SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        // if queryis empty then need a way to check if the query matched
        // create a thing
        EntityInstanceCollection thing = thingifier.getInstancesForSingularOrPluralNamedEntity(url, instanceDatabaseName);
        if (thing != null) {
            // can't create a new thing at root level with PUT
            return ApiResponse.error(405, "Cannot create root level entity with a PUT");
        }


        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length != 2) {
            // WHAT was that query?
            return ApiResponse.error(400, "Your request was not understood");
        }

        String thingName = urlParts[0];
        thing = thingifier.getInstancesForSingularOrPluralNamedEntity(thingName, instanceDatabaseName);

        if (thing == null) {
            // this is not a URL for thing/guid
            // unknown thing
            return NoSuchEntity.response(urlParts[0]);
        }

        String instanceGuid = urlParts[1];

        EntityInstance instance = thing.findInstanceByGUIDorID(instanceGuid);

        if (instance == null) {
            // it does not exist, but we have a GUID - create it
            // if we were given an ID then this will fail because
            // ID will not match GUID formatting
            return new ThingCreation(thingifier).withGuid(instanceGuid, args, thing, instanceDatabaseName);
        } else {
            // when amending existing thing with PUT it must be idempotent so
            // check that all fields are valid in the args
            return amendAThingWithPut(args, instance, instanceDatabaseName);
        }
    }

    private ApiResponse amendAThingWithPut(final BodyParser bodyargs, final EntityInstance instance, final String database) {

        return new ThingAmendment(thingifier).
                amendInstance(bodyargs, instance, true, database);
    }
}
