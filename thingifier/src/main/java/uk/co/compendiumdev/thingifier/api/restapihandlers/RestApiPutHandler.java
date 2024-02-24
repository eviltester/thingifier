package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

public class RestApiPutHandler {
    final Thingifier thingifier;

    public RestApiPutHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {

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

        EntityInstance instance = thing.findInstanceByPrimaryKey(instanceGuid);

        if (instance == null) {

            // cannot create on put when AUTO fields are present
            // if the primary key is an AUTO i.e. AUTO_GUID or AUTO_INCREMENT then we should not be able to create it
            // in fact if there are any fields at all which are AUTO then we should not be able to create it with PUT

            List<Field> forbiddenPutCreationFields = thing.definition().getFieldsOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            if(forbiddenPutCreationFields.size()>0){

                String names = "";
                for(Field field : forbiddenPutCreationFields){
                    if(!names.isEmpty()){
                        names = names + ", ";
                    }
                    names = names + field.getName();
                }
                return ApiResponse.error(400, String.format("Cannot create %s with PUT due to Auto fields %s", thing.definition().getName(), names));
            }


            // it does not exist, but we have a primary key - create it
            // any field in the body for the primary key must match the primarykey field
            List<NamedValue> fieldValues = FieldValues.fromListMapEntryStringString(args.getFlattenedStringMap());
            for( NamedValue namedValue : fieldValues){
                if(namedValue.name.equals(thing.definition().getPrimaryKeyField().getName())){
                    if(!namedValue.value.equals(instanceGuid)){
                        // primary key does not match the value in the message
                        return ApiResponse.error(400, String.format("Cannot create %s with PUT as key does not match body value %s != %s", thing.definition().getName(), instanceGuid, namedValue.value));
                    }
                }
            }


            // if we were given an ID then this will fail because
            // ID will not match GUID formatting
            return new ThingCreation(thingifier).withPrimaryKey(instanceGuid, args, thing, instanceDatabaseName);
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
