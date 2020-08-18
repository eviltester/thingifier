package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;

public class RestApiPostHandler {
    private final Thingifier thingifier;

    public RestApiPostHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final BodyParser args) {
        // we want to

        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamedSingularOrPlural(url);
        if (thing != null) {
            // create a new thing does not enforce relationships
            // TODO: validate before creation so as to only delete in an 'emergency' not as default
            final ApiResponse response = new ThingCreation(thingifier).with(args, thing);
            if(response.isErrorResponse()){
                return response;
            }

            ThingInstance returnedInstance = response.getReturnedInstance();
            final List<String> protectedFieldNames = returnedInstance.getEntity().getFieldNamesOfType(FieldType.ID, FieldType.GUID);
            ValidationReport validity = returnedInstance.validateFieldValues(protectedFieldNames, false);
            validity.combine(returnedInstance.validateRelationships());

            if(validity.isValid()){
                return response;
            }else{
                thingifier.deleteThing(response.getReturnedInstance());
                return ApiResponse.error(400, validity.getErrorMessages()).addToErrorMessages("No new item created");
            }
        }


        /*
            Match a specific thing
         */
        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length == 2) {

            String thingName = urlParts[0];
            thing = thingifier.getThingNamedSingularOrPlural(thingName);

            if (thing == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return NoSuchEntity.response(thingName);

            }

            String instanceGuid = urlParts[1];

            ThingInstance instance = thing.findInstanceByGUIDorID(instanceGuid);

            if (instance == null) {
                // cannot amend something that does not exist
                return ApiResponse.error404(String.format("No such %s entity instance with GUID or ID %s found", thing.definition().getName(), instanceGuid));
            }

            return amendAThingWithPost(args, instance);
        }


        /*
            Match a Relationship
         */
        // get the things to post to
        SimpleQuery query = new SimpleQuery(thingifier.getERmodel(), url).performQuery();
        if (query.lastMatchWasRelationship()) {
            return new RelationshipCreation(thingifier).create(url, args, query);
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }


    private ApiResponse amendAThingWithPost(BodyParser args, ThingInstance instance) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return new ThingAmendment(thingifier).amendInstance(args, instance, false);
    }


}
