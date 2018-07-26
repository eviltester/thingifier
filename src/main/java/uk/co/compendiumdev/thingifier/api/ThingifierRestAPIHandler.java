package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;

import java.util.HashMap;
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


    public ApiResponse post(final String url, final Map<String, String> args) {

        // we want to

        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamedSingularOrPlural(url);
        if (thing != null) {
            return createANewThingWith(args, thing);
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
            String instanceGuid = urlParts[1];

            return amendAThingWithPost(args, thing, thingName, instanceGuid);
        }


        /*
            Match a Relationship
         */
        // get the things to post to
        SimpleQuery query = new SimpleQuery(thingifier, url).performQuery();
        if (query.lastMatchWasRelationship()) {

            // get the relationship name
            String relationshipName = query.getLastRelationshipName();

            // find the thing in the body

            ThingInstance relatedItem = null;

            // if there is a guid in the body then use that to try and find a thing that matches it
            if (args.containsKey("guid")) {
                String thingGUID = (String) args.get("guid");

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

            // if we have a parent thing, but no GUID then can we create a Thing and connect it?
            if (relatedItem == null) {
                List<RelationshipVector> possibleRelationships = connectThis.getEntity().getRelationships(relationshipName);
                // if no way to narrow it down then use the first one TODO: potential bug if multiple named relationshps
                relationshipToUse = possibleRelationships.get(0);
                ThingDefinition createThing = relationshipToUse.getTo().definition();

                Thing thingToCreate = thingifier.getThingNamed(createThing.getName());

                final ApiResponse response = createANewThingWith(args, thingToCreate);
                if(response.isErrorResponse()){
                    return response;
                }else{
                    relatedItem = response.getReturnedInstance();
                    returnThing = relatedItem;
                }


            } else {
                // we know what we are connecting to, find the correct relationship
                relationshipToUse = connectThis.getEntity().getRelationship(relationshipName, relatedItem.getEntity());
            }


            try {

                if(relationshipToUse==null){
                    return ApiResponse.error(400, String.format("Could not find a relationship named %s between %s and a %s",
                            relationshipName,
                            connectThis.getEntity().getName(),
                            relatedItem.getEntity().getName()));

                }
                if(relationshipToUse.getTo().definition() != relatedItem.getEntity()){
                    return ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s because it is a %s instead of a %s",
                            connectThis.getGUID(), connectThis.getEntity().getName(),
                            relatedItem.getGUID(), relatedItem.getEntity().getName(),
                            relationshipToUse.getName(),
                            relatedItem.getEntity().getName(),
                            relationshipToUse.getTo().definition().getName()
                            ));
                }

                // TODO: enforce cardinality on relationship
                connectThis.connects(relationshipToUse.getName(), relatedItem);

            } catch (Exception e) {
                return ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s",
                        connectThis.getGUID(), connectThis.getEntity().getName(),
                        relatedItem.getGUID(), relatedItem.getEntity().getName(),
                        relationshipToUse.getName()));
            }


            return ApiResponse.created(returnThing);
        }


        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }



    private ApiResponse amendAThingWithPost(final Map<String, String> args, Thing thing, String thingName, String instanceGuid) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return amendAThing(args, thing, thingName, instanceGuid, false);
    }

    private ApiResponse amendAThingWithPut(final Map<String, String> args, final Thing thing, final ThingInstance instance) {
        return amendAThing(args, thing, thing.definition().getName(), instance.getGUID(), true);
    }

    private ApiResponse amendAThing(final Map<String, String> args, final Thing thing, final String thingName, final String instanceGuid, final Boolean clearFieldsBeforeSettingFromArgs) {
        if (thing == null) {
            // this is not a URL for thing/guid
            // unknown thing
            return noSuchEntity(thingName);

        }

        ThingInstance instance = thing.findInstanceByField(FieldValue.is("guid", instanceGuid));

        if (instance == null) {
            // cannot amend something that does not exist
            return ApiResponse.error404(String.format("No such %s entity instance with GUID %s found", thing.definition().getName(), instanceGuid));
        } else {

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
    }


    private ApiResponse createANewThingWith(final Map<String, String> args, final Thing thing) {
        // create a new thing
        ThingInstance instance;

        try {
            instance = thing.createInstance();
            instance.setFieldValuesFrom(args);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = instance.validate();

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

    public ApiResponse put(final String url, final Map<String, String> args) {


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

            thing = thingifier.getThingNamedSingularOrPlural(urlParts[0]);
            if (thing == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return noSuchEntity(urlParts[0]);
            }

            ThingInstance instance = thing.findInstanceByField(FieldValue.is("guid", urlParts[1]));

            if (instance == null) {
                // it does not exist, but we have a GUID - create it
                UUID aGUID;

                try {
                    aGUID = UUID.fromString(urlParts[1]);
                    instance = thing.createInstance(aGUID.toString());

                } catch (Exception e) {
                    // that is not a valid guid
                    System.out.println(e.getMessage());
                    return ApiResponse.error404(String.format("Invalid GUID for %s entity %s", urlParts[1], urlParts[0]));
                }

                try {
                    instance.setFieldValuesFrom(args);
                } catch (Exception e) {
                    return ApiResponse.error(400, e.getMessage());
                }


                ValidationReport validation = instance.validate();

                if (validation.isValid()) {
                    thing.addInstance(instance);
                    return ApiResponse.created(instance);
                } else {
                    // do not add it, report the errors
                    return ApiResponse.error(400, validation.getErrorMessages());
                }


            } else {
                // when amending existing thing with PUT it must be idempotent so
                // check that all fields are valid in the args
                return amendAThingWithPut(args, thing, instance);

            }
        }


        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");


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
                thingifier.getThingNamed(instance.getEntity().getName()).deleteInstance(instance.getGUID());
            }

        }
        return ApiResponse.success();
    }

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
}
