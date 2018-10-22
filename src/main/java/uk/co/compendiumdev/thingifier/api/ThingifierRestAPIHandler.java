package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThingifierRestAPIHandler {
    private final Thingifier thingifier;

    public ThingifierRestAPIHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }


    // TODO: we should be able to accept xml with correct content type
    // TODO: we should be able to accept html forms with correct content type
    // todo allow an accept text/html to create different output - (probably handled by routings rather than api)
    // TODO : this whole class needs to be refactored and wrapped with unit tests
    // todo : generate examples when outputing the api documentation

    // TODO: - listed here https://www.lisihocke.com/2018/07/testing-tour-stop-16-pair-exploring-an-api-with-thomas.html
    // TODO: ensure that relationshps enforce the type of thing e.g. if I pass in a GUID of the wrong type then it should not cross ref
    // TODO: possibly consider an X- header which has the number of items in the collection

    public ApiResponse get(final String url) {


        SimpleQuery queryResults = new SimpleQuery(thingifier, url).performQuery();
        List<ThingInstance> queryItems = queryResults.getListThingInstance();

        // return a 404 if it doesn't match anything
        if (queryResults.lastMatchWasNothing() ||
                (queryResults.lastMatchWasInstance() && queryItems.size() == 0)) {
            // if query list was empty then return a 404
            return ApiResponse.error404(String.format("Could not find an instance with %s", url));
        }

        if (queryResults.lastMatchWasInstance()) {
            if (queryResults.isResultACollection()) {
                // if we asked for /projects then we should always return a collection
                return ApiResponse.success().returnInstanceCollection(queryResults.getListThingInstance());
            } else {
                return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance());
            }
        } else {

            return ApiResponse.success().returnInstanceCollection(queryItems).resultContainsType(queryResults.resultContainsDefn());
        }
    }

    public ApiResponse delete(final String url) {
        // this should probably not delete root items
        Thing thing = thingifier.getThingNamedSingularOrPlural(url);
        if (thing != null) {
            // can't delete root level with a DELETE
            return ApiResponse.error(405, "Cannot delete root level entity");
        }

        SimpleQuery queryresult = new SimpleQuery(thingifier, url).performQuery();

        if (queryresult.wasItemFoundUnderARelationship()) {
            // delete the relationships not the items
            ThingInstance parent = queryresult.getParentInstance();
            ThingInstance child = queryresult.getLastInstance();
            parent.removeRelationshipsTo(child, queryresult.getLastRelationshipName());
        } else {
            List<ThingInstance> items = queryresult.getListThingInstance();
            if (items.size() == 0) {
                // 404 not found - nothing to delete
                return ApiResponse.error404(String.format("Could not find any instances with %s", url));
            }
            for (ThingInstance instance : items) {
                thingifier.deleteThing(instance);
            }

        }
        return ApiResponse.success();
    }



    public ApiResponse post(final String url, final BodyParser args) {

        // we want to

        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamedSingularOrPlural(url);
        if (thing != null) {
            // create a new thing does not enforce relationships
            final ApiResponse response = createANewThingWith(args.getStringMap(), thing);
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
                return noSuchEntity(thingName);

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
            return createRelationship(url, args.getStringMap(), query);
        }


        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }

    public ApiResponse put(final String url, final BodyParser args) {


        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamedSingularOrPlural(url);
        if (thing != null) {
            // can't create a new thing at root level with PUT
            return ApiResponse.error(405, "Cannot create root level entity with a PUT");
        }


        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length == 2) {

            String thingName = urlParts[0];
            thing = thingifier.getThingNamedSingularOrPlural(thingName);

            if (thing == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return noSuchEntity(urlParts[0]);
            }

            String instanceGuid = urlParts[1];

            ThingInstance instance = thing.findInstanceByField(FieldValue.is("guid", instanceGuid));

            if (instance == null) {
                // it does not exist, but we have a GUID - create it

                return createANewThingWithGuid(instanceGuid, args.getStringMap(), thing);


            } else {
                // when amending existing thing with PUT it must be idempotent so
                // check that all fields are valid in the args
                return amendAThingWithPut(args.getStringMap(), instance);

            }
        }


        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");


    }


    private ApiResponse createRelationship(final String url, final Map<String, String> args, final SimpleQuery query) {
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
            List<RelationshipVector> possibleRelationships = connectThis.getEntity().getRelationships(relationshipName);
            // if no way to narrow it down then use the first one TODO: potential bug if multiple named relationshps
            relationshipToUse = possibleRelationships.get(0);
            ThingDefinition createThing = relationshipToUse.getTo().definition();

            thingToCreate = thingifier.getThingNamed(createThing.getName());

            response = createANewThingWith(args, thingToCreate);
            if(response.isErrorResponse()){
                return response;
            }else{
                // Created it, so relate it later
                relatedItem = response.getReturnedInstance();
                returnThing = relatedItem;
            }


        } else {
            // we know what we are connecting to, find the correct relationship
            relationshipToUse = connectThis.getEntity().getRelationship(relationshipName, relatedItem.getEntity());
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

            connectThis.connects(relationshipToUse.getName(), relatedItem);

            // enforce cardinality on relationship
            ValidationReport validNow = relatedItem.validate();
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


        return ApiResponse.created(returnThing);
    }




    private ApiResponse amendAThingWithPost(final Map<String, String> args, ThingInstance instance) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return amendAThingInstance(args, instance, false);
    }

    private ApiResponse amendAThingWithPut(final Map<String, String> args, final ThingInstance instance) {
        return amendAThingInstance(args, instance, true);
    }

    private ApiResponse amendAThingInstance(final Map<String, String> args, final ThingInstance instance, final Boolean clearFieldsBeforeSettingFromArgs) {


            ThingInstance cloned = null;

            try {

                cloned = instance.createDuplicateWithoutRelationships();

                if(clearFieldsBeforeSettingFromArgs){
                    // if you want an idempotent amend then clear it down prior to amending
                    cloned.clearAllFields();
                }
                cloned.setFieldValuesFrom(args);

            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }

            ValidationReport validation = cloned.validate();

            if (validation.isValid()) {
                if(clearFieldsBeforeSettingFromArgs){
                    instance.clearAllFields();
                }
                instance.setFieldValuesFrom(args);
                return ApiResponse.success().returnSingleInstance(instance);
            } else {
                // do not add it, report the errors
                return ApiResponse.error(400, validation.getErrorMessages());
            }

    }

    private ApiResponse createANewThingWithGuid(final String instanceGuid, final Map<String, String> args, final Thing thing) {

        ThingInstance instance;

        try {
            String aGUID = UUID.fromString(instanceGuid).toString();
            instance = thing.createInstance(aGUID);

        } catch (Exception e) {
            // that is not a valid guid
            System.out.println(e.getMessage());
            return ApiResponse.error404(String.format("Invalid GUID for %s entity %s", instanceGuid, thing.definition().getName()));
        }

        return addNewThingWithFields(args, instance, thing);
    }

    private ApiResponse createANewThingWith(final Map<String, String> args, final Thing thing) {
        return addNewThingWithFields(args, thing.createInstance(), thing);
    }

    /**
     * Because we are creating a new thing we can only validate fields and not the relationships
     *
     * @param args
     * @param instance
     * @param thing
     * @return
     */
    private ApiResponse addNewThingWithFields(final Map<String, String> args, final ThingInstance instance, final Thing thing) {

        try {
            instance.setFieldValuesFrom(args);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = instance.validateFields();

        if (validation.isValid()) {
            thing.addInstance(instance);
            return ApiResponse.created(instance);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }

    private ApiResponse noSuchEntity(final String entityName) {
        return ApiResponse.error404(String.format("No such entity as %s found", entityName));
    }





}
