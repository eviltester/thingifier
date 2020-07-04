package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {



    /**
     * This is more suitable for JSON output of an array
     * @param things
     * @param typeName
     * @return
     */
    public static String asJsonTypedArrayWithContentsUntyped(final List<ThingInstance> things, String typeName) {
        final JsonObject arrayObj = new JsonObject();
        arrayObj.add(typeName, asJsonArray(things));
        return arrayObj.toString();
    }

    /**
     * This is suitable only for internal use - Json output of an array should always have a wrapper name e.g. {todos: []}
     * @param things
     * @return
     */
    private static JsonArray asJsonArray(final Collection<ThingInstance> things) {

        // [{"guid":"bob"}, {"guid":"bob2"}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {
            jsonArray.add(asJsonObject(thing));
        }

        //System.out.println(jsonArray.toString());
        return jsonArray;
    }

    /**
     * Suitable for JSON Output as it is just the object
     * @param thingInstance
     * @return
     */
    public static JsonObject asJsonObject(final ThingInstance thingInstance) {

        final JsonObject jsonobj = new JsonObject();

        if (thingInstance == null) {
            return jsonobj;
        }

        for (String field : thingInstance.getEntity().getFieldNames()) {
            jsonobj.addProperty(field, thingInstance.getValue(field));
        }

        // TODO: add relationships
        // TODO: add tests for relationship rendering
        /*
            "relationships" : [
                {
                    "relationship_name" : [
                        {
                            "typeofthingsplural": [
                                {"guid" : "value"}
                            ]
                        }
                    ]
                }
            ]
         */
        // TODO: consider if we should just make the "relationship_name": [{"guid","value"},{...}] array the root
        final Collection<RelationshipVector> relationships = thingInstance.getEntity().getRelationships();
        // "relationships" : [
        if(relationships.size()>0){
            final JsonArray relationshipsArray = new JsonArray();

            // fill the array "relationship_name" : [
            for(RelationshipVector relationship : relationships){
                final Collection<ThingInstance> relatedItems = thingInstance.connectedItems(relationship.getName());
                if(relatedItems.size()>0) {
                    // relationship_name" : [
                    final JsonArray namedRelationshipInstancesArray = new JsonArray();

                    // for each thing related to
                    //"typeofthingsplural": [
                    final JsonArray arrayOfGuids = new JsonArray();
                    for(ThingInstance item : relatedItems) {
                        final JsonObject itemGuidObject = new JsonObject();
                        itemGuidObject.addProperty("guid", item.getGUID());
                        arrayOfGuids.add(itemGuidObject);
                    }

                    //"typeofthingsplural": [
                    final JsonObject objectForArrayOfGuids = new JsonObject();
                    objectForArrayOfGuids.add(relationship.getTo().definition().getPlural(), arrayOfGuids);
                    namedRelationshipInstancesArray.add(objectForArrayOfGuids);


                    // relationship_name" : [
                    final JsonObject relationshipArrayObject = new JsonObject();
                    relationshipArrayObject.add(relationship.getName(), namedRelationshipInstancesArray);
                    relationshipsArray.add(relationshipArrayObject);
                }
            }

            jsonobj.add("relationships", relationshipsArray);
        }

        return jsonobj;
    }

    /**
     * This is more suitable for XML output
     * @param things
     * @param defn
     * @return
     */
    public static String asJsonTypedArrayWithContentsTyped(final List<ThingInstance> things, ThingDefinition defn) {

        final JsonObject arrayObj = new JsonObject();
        arrayObj.add(defn.getPlural(), asJsonArrayInstanceWrapped(things));
        return arrayObj.toString();
    }


    /**
     * This is suitable for partial XML output but should never be used directly as it needs a wrapper name to make sense
     * @param things
     * @return
     */
    private static JsonArray asJsonArrayInstanceWrapped(Collection<ThingInstance> things) {


        // [{"todo":{"guid":"bob"}}, {"todo":{"guid":"bob2"}}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {

            JsonObject jsonObj = new JsonObject();
            jsonObj.add(thing.getEntity().getName(), asJsonObject(thing));
            jsonArray.add(jsonObj);

        }

        //System.out.println(jsonArray.toString());
        return jsonArray;
    }




    /**
     *   Suitable for XML output as it has a name
     */
    public static JsonObject asNamedJsonObject(final ThingInstance instance) {

        final JsonObject retObj = new JsonObject();
        retObj.add(instance.getEntity().getName(), JsonThing.asJsonObject(instance));
        return retObj;

    }

}
