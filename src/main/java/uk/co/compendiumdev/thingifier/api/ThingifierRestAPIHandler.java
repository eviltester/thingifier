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

    // TODO: could we simplify all XML and JSON conversion on output create a MAP and convert this to XML or JSON?

    // TODO: - listed here https://www.lisihocke.com/2018/07/testing-tour-stop-16-pair-exploring-an-api-with-thomas.html
    // TODO: ensure that relationshps enforce the type of thing e.g. if I pass in a GUID of the wrong type then it should not cross ref
    // TODO: possibly consider an X- header which has the number of items in the collection
    // TODO: consider empty json collection having the type e.g. {"todos": []}



    public ApiResponse post(final String url, final Map args) {


        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamed(url);
        if (thing != null) {
            return createANewThingWithPost(args, thing);
        }


        /*
            Match a specific thing
         */
        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length == 2) {

            String thingName = urlParts[0];
            thing = thingifier.getThingNamed(thingName);
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

                try {
                    relatedItem = thingToCreate.createInstance().setFieldValuesFrom(stringMap(args));
                } catch (Exception e) {
                    return ApiResponse.error(400, e.getMessage());
                }


                // assuming the thing is valid - it might not be so detect it here
                ValidationReport validation = relatedItem.validate();

                if (validation.isValid()) {
                    thingToCreate.addInstance(relatedItem);
                    returnThing = relatedItem;
                } else {
                    // do not add it, report the errors
                    return ApiResponse.error(400, validation.getErrorMessages());
                }
            } else {
                // we know what we are connecting to, find the correct relationship
                relationshipToUse = connectThis.getEntity().getRelationship(relationshipName, relatedItem.getEntity());
            }


            try {
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

        // Assume it matches  alist


        List<ThingInstance> items = query.getListThingInstance();

        if (items.size() > 0) {
            // TODO: this should really have validation
            // TODO: not implemented yet
            // this should be creating a new instance of the type of thing with a relationship to the parent
            // simple query needs to support looking at the things it found e.g. matched "todo" thing, get parent for the todo (a project), copy the relationships from the parent to the todo
            return ApiResponse.error(501, "Amending multiple items is not supported");
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }

    private Map<String, String> stringMap(final Map<String, Object> args) {
        Map<String, String> stringsInMap = new HashMap();
        for (String key : args.keySet()){
            if (args.get(key) instanceof String){
                stringsInMap.put(key, (String) args.get(key));
            }
        }
        return stringsInMap;
    }

    private ApiResponse amendAThingWithPost(Map args, Thing thing, String thingName, String instanceGuid) {
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

                cloned.setFieldValuesFrom(args);

            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }

            ValidationReport validation = cloned.validate();

            if (validation.isValid()) {
                instance.setFieldValuesFrom(args);
                return ApiResponse.success().returnSingleInstance(instance);
            } else {
                // do not add it, report the errors
                return ApiResponse.error(400, validation.getErrorMessages());
            }
        }
    }

    private ApiResponse createANewThingWithPost(Map args, Thing thing) {
        // create a new thing
        ThingInstance instance = thing.createInstance();

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
    }

    private ApiResponse noSuchEntity(String entityName) {
        return ApiResponse.error404(String.format("No such entity as %s found", entityName));
    }

    public ApiResponse put(String url, Map args) {





        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamed(url);
        if (thing != null) {
            // can't create a new thing at root level with PUT
            return ApiResponse.error(405, "Cannot create root level entity with a PUT");
        }


        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length == 2) {

            thing = thingifier.getThingNamed(urlParts[0]);
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

                ThingInstance cloned;

                try {

                    // create copy and validate copy
                    cloned = instance.createDuplicateWithoutRelationships();

                    // quick hack to make idempotent - delete all values and add new ones
                    cloned.clearAllFields(); // except "guid"

                    cloned.setFieldValuesFrom(args);

                    ValidationReport validation = cloned.validate();

                    if (validation.isValid()) {
                        instance.clearAllFields();
                        instance.setFieldValuesFrom(args);
                        return ApiResponse.success().returnSingleInstance(instance);
                    } else {
                        // do not add it, report the errors
                        return ApiResponse.error(400, validation.getErrorMessages());
                    }

                } catch (Exception e) {
                    return ApiResponse.error(400, e.getMessage());
                }



            }
        }

        // Assume it matches  alist

        // get the things to post to
        List<ThingInstance> query = thingifier.simplequery(url);

        if (query.size() > 0) {
            // TODO: not implemented yet
            // this should be creating a new instance of the type of thing with a relationship to the parent
            // simple query needs to support looking at the things it found e.g. matched "todo" thing, get parent for the todo (a project), copy the relationships from the parent to the todo
            return ApiResponse.error(501, "Amending multiple items is not supported");
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");


    }

    public ApiResponse delete(String url) {
        // this should probably not delete root items
        Thing thing = thingifier.getThingNamed(url);
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
            return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance()).resultContainsType(queryResults.resultContainsDefn());
        } else {
            return ApiResponse.success().returnInstanceCollection(queryItems).resultContainsType(queryResults.resultContainsDefn());
        }
    }
}
