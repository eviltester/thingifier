package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

public class RelationshipCreation {

    private final Thingifier thingifier;

    public RelationshipCreation(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse create(final String url, final BodyParser bodyargs, final SimpleQuery query) {

        final Map<String, String> args = bodyargs.getStringMap();

        // get the relationship name
        String relationshipName = query.getLastRelationshipName();

        // find the thing in the body

        ThingInstance relatedItem = null;

        // if there is a guid in the body then use that to try and find a thing that matches it
        if (args.containsKey("guid")) {
            String thingGUID = args.get("guid");

            relatedItem = thingifier.findThingInstanceByGuid(thingGUID);
            if (relatedItem == null) {
                return ApiResponse.error404(String.format("Could not find thing with GUID %s", thingGUID));
            }
        }


        // find the thing from the query to connect the relatedItem to
        ThingInstance connectThis = query.getParentInstance();
        if (connectThis == null) {
            // TODO: I don't think it is possible to ever hit this line of code
            return ApiResponse.error404(String.format("Could not find parent thing for relationship %s", url));
        }


        ThingInstance returnThing = null;

        RelationshipVector relationshipToUse;
        Thing thingToCreate = null;
        ApiResponse response = null;

        // if we have a parent thing, but no GUID then can we create a Thing and connect it later?
        if (relatedItem == null) {
            List<RelationshipVector> possibleRelationships = connectThis.getEntity().related().getRelationships(relationshipName);
            // if no way to narrow it down then use the first one TODO: potential bug if multiple named relationshps
            relationshipToUse = possibleRelationships.get(0);
            ThingDefinition createThing = relationshipToUse.getTo().definition();

            thingToCreate = thingifier.getThingNamed(createThing.getName());

            response = new ThingCreation(thingifier).with(bodyargs, thingToCreate);
            if(response.isErrorResponse()){
                return response;
            }else{
                // Created it, so relate it later
                relatedItem = response.getReturnedInstance();
                returnThing = relatedItem;
            }


        } else {
            // we know what we are connecting to, find the correct relationship
            relationshipToUse = connectThis.getEntity().getNamedRelationshipTo(relationshipName, relatedItem.getEntity());
//            relationshipToUse = connectThis.getEntity().related().getRelationship(relationshipName, relatedItem.getEntity());
        }


        try {

            if(relationshipToUse==null){
                response = ApiResponse.error(400, String.format("Could not find a relationship named %s between %s and a %s",
                        relationshipName,
                        connectThis.getEntity().getName(),
                        relatedItem.getEntity().getName()));

            }else {
                if (relationshipToUse.getTo().definition() != relatedItem.getEntity()) {
                    response = ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s because it is a %s instead of a %s",
                            connectThis.getGUID(), connectThis.getEntity().getName(),
                            relatedItem.getGUID(), relatedItem.getEntity().getName(),
                            relationshipToUse.getName(),
                            relatedItem.getEntity().getName(),
                            relationshipToUse.getTo().definition().getName()
                    ));
                }
            }

            if(response != null && response.isErrorResponse()){
                if(thingToCreate != null){
                    // we had an error so delete the created thing
                    thingifier.deleteThing(relatedItem);
                    response.addToErrorMessages(" the newly created item was deleted. No new items have been created.");

                }
                // we already have an error so return now
                return response;
            }

            connectThis.getRelationships().connect(relationshipToUse.getName(), relatedItem);

            // enforce cardinality on relationship
            ValidationReport validNow = relatedItem.validateRelationships();
            if(!validNow.isValid()){
                response = ApiResponse.error(400, validNow.getErrorMessages());
                thingifier.deleteThing(relatedItem);
                return response;
            }

        } catch (Exception e) {
            return ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s",
                    connectThis.getGUID(), connectThis.getEntity().getName(),
                    relatedItem.getGUID(), relatedItem.getEntity().getName(),
                    relationshipToUse.getName()));
        }


        return ApiResponse.created(returnThing, thingifier.apiConfig());
    }
}
