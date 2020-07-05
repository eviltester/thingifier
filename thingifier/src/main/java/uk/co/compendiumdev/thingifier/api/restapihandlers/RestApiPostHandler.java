package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;

import java.util.Map;

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
            final ApiResponse response = new ThingCreation(thingifier).with(args.getStringMap(), thing);
            if(response.isErrorResponse()){
                return response;
            }

            ValidationReport validity = response.getReturnedInstance().validate();
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

            ThingInstance instance = thing.findInstanceByField(FieldValue.is("guid", instanceGuid));

            if (instance == null) {
                // cannot amend something that does not exist
                return ApiResponse.error404(String.format("No such %s entity instance with GUID %s found", thing.definition().getName(), instanceGuid));
            }

            return amendAThingWithPost(args.getStringMap(), instance);
        }


        /*
            Match a Relationship
         */
        // get the things to post to
        SimpleQuery query = new SimpleQuery(thingifier, url).performQuery();
        if (query.lastMatchWasRelationship()) {
            return new RelationshipCreation(thingifier).create(url, args.getStringMap(), query);
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }


    private ApiResponse amendAThingWithPost(final Map<String, String> args, ThingInstance instance) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return new ThingAmendment(thingifier).amendInstance(args, instance, false);
    }


}
