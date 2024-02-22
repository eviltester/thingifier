package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

public class RelationshipCreation {

    private final Thingifier thingifier;

    public RelationshipCreation(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse create(final String url, final BodyParser bodyargs, final SimpleQuery query, final String database) {

        final Map<String, String> args = bodyargs.getStringMap();

        // get the relationship name
        String relationshipName = query.getLastRelationshipName();

        // find the thing in the body

        EntityInstance relatedItem = null;

        // find the thing from the query to connect the relatedItem to
        EntityInstance connectThis = query.getParentInstance();
        if (connectThis == null) {
            // TODO: I don't think it is possible to ever hit this line of code
            return ApiResponse.error404(String.format("Could not find parent thing for relationship %s", url));
        }

        RelationshipVectorDefinition relationshipToUse;
        List<RelationshipVectorDefinition> possibleRelationships = connectThis.getEntity().related().getRelationships(relationshipName);
        // if no way to narrow it down then use the first one TODO: potential bug if multiple named relationshps
        relationshipToUse = possibleRelationships.get(0);

        EntityInstanceCollection thingTo = thingifier.getThingInstancesNamed(relationshipToUse.getTo().getName(), database);

        // if there is a guid in the body then use that to try and find a thing that matches it
        // if there is a guid field or an id field then use whichever first matches a thing
        boolean amExpectingARelatedItem = false;
        String matchingFieldNames = "";
        for(String fieldName : args.keySet()){
            final Field field = thingTo.definition().getField(fieldName);
            // in theory this is any 'key' unique field
            if(field.getType()== FieldType.AUTO_GUID || field.getType() == FieldType.AUTO_INCREMENT){
                amExpectingARelatedItem=true;
                if(!matchingFieldNames.contains(fieldName+ " ")){
                    matchingFieldNames = matchingFieldNames + fieldName +" ";
                }
                relatedItem = thingTo.findInstanceByFieldNameAndValue(fieldName, args.get(fieldName));
                if(relatedItem!=null){
                    // found something
                    break;
                }
            }
        }
        if(amExpectingARelatedItem && relatedItem==null){
            matchingFieldNames = matchingFieldNames.trim().replace(" ", ", ");
            return ApiResponse.error404(String.format("Could not find thing matching value for %s", matchingFieldNames));
        }

        EntityInstance returnThing = null;

        EntityInstanceCollection thingToCreate = null;
        ApiResponse response = null;

        // if we have a parent thing, but no GUID then can we create a Thing and connect it later?
        if (relatedItem == null) {
            EntityDefinition createThing = relationshipToUse.getTo();

            thingToCreate = thingifier.getThingInstancesNamed(createThing.getName(), database);

            response = new ThingCreation(thingifier).with(bodyargs, thingToCreate, database);
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
                if (relationshipToUse.getTo() != relatedItem.getEntity()) {
                    response = ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s because it is a %s instead of a %s",
                            connectThis.getPrimaryKeyValue(), connectThis.getEntity().getName(),
                            relatedItem.getPrimaryKeyValue(), relatedItem.getEntity().getName(),
                            relationshipToUse.getName(),
                            relatedItem.getEntity().getName(),
                            relationshipToUse.getTo().getName()
                    ));
                }
            }

            if(response != null && response.isErrorResponse()){
                if(thingToCreate != null){
                    // we had an error so delete the created thing
                    thingifier.deleteThing(relatedItem, database);
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
                thingifier.deleteThing(relatedItem, database);
                return response;
            }

        } catch (Exception e) {
            return ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s",
                    connectThis.getPrimaryKeyValue(), connectThis.getEntity().getName(),
                    relatedItem.getPrimaryKeyValue(), relatedItem.getEntity().getName(),
                    relationshipToUse.getName()));
        }


        return ApiResponse.created(returnThing, thingifier.apiConfig());
    }
}
