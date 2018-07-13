package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThingifierRestAPIHandler {
    private final Thingifier thingifier;

    public ThingifierRestAPIHandler(Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    // TODO: should not return boolean should return an HTTP status and an error message or the item
    public ApiResponse post(String url, String body) {

        Map args = new Gson().fromJson(body, Map.class);



        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamed(url);
        if(thing!=null){
            // create a new thing
            ThingInstance instance = thing.createInstance();
            instance.setFieldValuesFrom(args);
            // TODO check if it is valid
            thing.addInstance(instance);
            return ApiResponse.created(JsonThing.asJson(instance));
        }


        /*
            Match a specific thing
         */
        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if(urlParts.length==2) {

            thing = thingifier.getThingNamed(urlParts[0]);
            if (thing == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return noSuchEntity(urlParts[0]);

            }
            ThingInstance instance = thing.findInstance(FieldValue.is("guid", urlParts[1]));
            if (instance==null){
                // cannot amend something that does not exist
                return ApiResponse.error404(String.format("Nothing such %s entity with GUID %s found", urlParts[0], urlParts[1]));
            }else{
                instance.setFieldValuesFrom(args);
                return ApiResponse.success(JsonThing.asJson(instance));
            }
        }


        /*
            Match a Relationship
         */
        // get the things to post to
        SimpleQuery query = new SimpleQuery(thingifier, url).performQuery();
        if(query.lastMatchWasRelationship()){

            // get the relationship name
            String relationshipName = query.getLastRelationshipName();

            // find the thing in the body

            ThingInstance relatedItem=null;

            // if there is a guid in the body then use that to try and find a thing that matches it
            JsonObject thingJson= new JsonParser().parse(body).getAsJsonObject();
            if(thingJson.has("guid")) {
                String thingGUID = thingJson.get("guid").getAsString();

                relatedItem = thingifier.findThingInstanceByGuid(thingGUID);
                if (relatedItem == null) {
                    return ApiResponse.error404(String.format("Could not find thing with GUID %s", thingGUID));
                }
            }


            // find the thing from the query to connect the relatedItem to
            ThingInstance connectThis = query.getParentInstance();
            if(connectThis==null){
                return ApiResponse.error404(String.format("Could not find parent thing for relationship %s", url));
            }


            ThingInstance returnThing = connectThis;

            // if we have a parent thing, but no GUID then can we create a Thing and connect it?
            if(relatedItem==null) {
                List<RelationshipDefinition> possibleRelationships = connectThis.getEntity().getRelationships(relationshipName);
                // if no way to narrow it down then use the first one
                RelationshipDefinition relationshipToUse = possibleRelationships.get(0);
                ThingDefinition createThing = relationshipToUse.to();

                Thing thingToCreate = thingifier.getThingNamed(createThing.getName());
                relatedItem = thingToCreate.createInstance().setFieldValuesFrom(new Gson().fromJson(body, Map.class));

                // TODO: assuming the thing is valid - it might not be so fix it here
                thingToCreate.addInstance(relatedItem);
                returnThing = relatedItem;
            }



            try {
                connectThis.connects(relationshipName, relatedItem);
            }catch(Exception e){
                return ApiResponse.error(400, String.format("Could not connect %s (%s) to %s (%s) via relationship %s",
                                                                        connectThis.getGUID(), connectThis.getEntity().getName(),
                                                                        relatedItem.getGUID(), relatedItem.getEntity().getName(),
                                                                        relationshipName));
            }

            // TODO: 201/Created should have a Location header which points to the API call to get the thing created
            return ApiResponse.created(JsonThing.asJson(returnThing));
        }

        // Assume it matches  alist



        List<ThingInstance> items = query.getListThingInstance();

        if(items.size()>0) {
            // TODO: this should really have validation
            // TODO: not implemented yet
            // this should be creating a new instance of the type of thing with a relationship to the parent
            // simple query needs to support looking at the things it found e.g. matched "todo" thing, get parent for the todo (a project), copy the relationships from the parent to the todo
            return ApiResponse.error(501, "Amending multiple items is not supported");
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }

    private ApiResponse noSuchEntity(String entityName) {
        return ApiResponse.error404(String.format("Nothing such entity as %s found", entityName));
    }

    public ApiResponse put(String url, String body) {


        Map args = new Gson().fromJson(body, Map.class);


        // if queryis empty then need a way to check if the query matched
        // create a thing
        Thing thing = thingifier.getThingNamed(url);
        if(thing!=null){
            // can't create a new thing at root level with PUT
            return ApiResponse.error(405, "Cannot create root level entity with a PUT");
        }


        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if(urlParts.length==2) {

            thing = thingifier.getThingNamed(urlParts[0]);
            if (thing == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return noSuchEntity(urlParts[0]);
            }
            ThingInstance instance = thing.findInstance(FieldValue.is("guid", urlParts[1]));

            if (instance==null){
                // it does not exist, but we have a GUID - create it
                UUID aGUID;

                try {
                    aGUID = UUID.fromString(urlParts[1]);
                    instance = thing.createInstance(aGUID.toString());
                    instance.setFieldValuesFrom(args);
                    // TODO check if it is valid
                    thing.addInstance(instance);
                    return ApiResponse.created(JsonThing.asJson(instance));

                }catch(Exception e){
                    // that is not a valid guid
                    System.out.println(e.getMessage());
                    return ApiResponse.error404(String.format("Nothing such %s entity with GUID %s found", urlParts[0], urlParts[1]));
                }
            }else{
                // when amending existing thing with PUT it must be idempotent so
                // TODO: check that all fields are valid in the args
                // quick hack to make idempotent - delete all values and add new ones
                instance.clearAllFields(); // except "guid"
                instance.setFieldValuesFrom(args);
                return ApiResponse.success(JsonThing.asJson(instance));
            }
        }

        // Assume it matches  alist

        // get the things to post to
        List<ThingInstance> query = thingifier.simplequery(url);

        if(query.size()>0) {
            // TODO: this should really have validation
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
        if(thing!=null){
            // can't delete root level with a DELETE
            return ApiResponse.error(405, "Cannot delete root level entity");
        }

        SimpleQuery queryresult = new SimpleQuery(thingifier, url).performQuery();

        if(queryresult.wasItemFoundUnderARelationship()){
            // delete the relationships not the items
            ThingInstance parent = queryresult.getParentInstance();
            ThingInstance child = queryresult.getLastInstance();
            parent.removeRelationshipsTo(child, queryresult.getLastRelationshipName());
        }else{
            List<ThingInstance> items = queryresult.getListThingInstance();
            if(items.size()==0){
                // TODO 404 not found - nothing to delete
                return ApiResponse.error404(String.format("Could not find any instances with %s", url));
            }
            for(ThingInstance instance : items){
                thingifier.getThingNamed(instance.getEntity().getName()).deleteInstance(instance.getGUID());
            }

        }
        return ApiResponse.success("");
    }

    public ApiResponse get(String url) {
        // should really check for 404 with it doesn't match anything
        List<ThingInstance> query = thingifier.simplequery(url);
        return ApiResponse.success(JsonThing.asJson(query));
    }
}
