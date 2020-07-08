package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import uk.co.compendiumdev.thingifier.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {


    private final JsonOutputConfig apiConfig;

    public JsonThing(final JsonOutputConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * This is more suitable for JSON output of an array
     * @param things
     * @param typeName
     * @return
     */
    public String asJsonTypedArrayWithContentsUntyped(final List<ThingInstance> things, String typeName) {
        final JsonObject arrayObj = new JsonObject();
        arrayObj.add(typeName, asJsonArray(things));
        return arrayObj.toString();
    }

    /**
     * This is suitable only for internal use - Json output of an array should always have a wrapper name e.g. {todos: []}
     * @param things
     * @return
     */
    private JsonArray asJsonArray(final Collection<ThingInstance> things) {

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
    public JsonObject asJsonObject(final ThingInstance thingInstance) {

        final JsonObject jsonobj = new JsonObject();

        if (thingInstance == null) {
            return jsonobj;
        }

        for (String field : thingInstance.getEntity().getFieldNames()) {
            try {
                jsonobj.addProperty(field, thingInstance.getValue(field));
            }catch(Exception e){
                // ignore
            }
        }

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
        final Collection<RelationshipVector> relationships = thingInstance.getEntity().getRelationships();

        // compressed relationships are possible when relationship_name is not a field name
        /*
            task-of: [{"guid":"..."},{...}]
         */
        Boolean hasAnyComplexRelationships = false; // assume that most relationships can be compressed

        // config of output
        Boolean allowCompressedRelationships = apiConfig.doesAllowCompressedRelationships();
        Boolean useIdsInRelationshipRenderingIfAvailable = apiConfig.doesRelationshipsUseIdsIfAvailable(); // todo: allow configuring relationship rendering at an app or api level

        // "relationships" : [
        if(relationships.size()>0 && thingInstance.hasAnyRelationshipInstances()){
            final JsonArray relationshipsArray = new JsonArray();

            // fill the array "relationship_name" : [
            for(RelationshipVector relationship : relationships){
                final Collection<ThingInstance> relatedItems = thingInstance.connectedItems(relationship.getName());

                boolean isCompressedRelationship=true;
                if(thingInstance.getEntity().hasFieldNameDefined(relationship.getName())){
                    // cannot make compressed because it has a field of the same name
                    isCompressedRelationship=false;
                }

                if(relatedItems.size()>0) {

                    // for each thing related to
                    //"typeofthingsplural": [
                    final JsonArray arrayOfGuids = new JsonArray();
                    for(ThingInstance item : relatedItems) {
                        final JsonObject itemGuidObject = new JsonObject();

                        String fieldNameAsUniqueId = "guid";
                        String valueOfUniqueId = item.getGUID();

                        if(useIdsInRelationshipRenderingIfAvailable){
                            if(item.hasIDField()){
                                fieldNameAsUniqueId = item.getEntity().getIDField().getName();
                                valueOfUniqueId = item.getValue(fieldNameAsUniqueId);
                            }
                        }
                        itemGuidObject.addProperty(fieldNameAsUniqueId, valueOfUniqueId);

                        arrayOfGuids.add(itemGuidObject);
                    }

                    if(isCompressedRelationship && allowCompressedRelationships){
                        // if it is compressed then add the array directly to the jsonobj
                        // relationship_name" : [
                        jsonobj.add(relationship.getName(), arrayOfGuids);

                    }else {

                        final JsonArray namedRelationshipInstancesArray = new JsonArray();
                        //"typeofthingsplural": [
                        final JsonObject objectForArrayOfGuids = new JsonObject();
                        objectForArrayOfGuids.add(relationship.getTo().definition().getPlural(), arrayOfGuids);
                        namedRelationshipInstancesArray.add(objectForArrayOfGuids);

                        // relationship_name" : [
                        final JsonObject relationshipArrayObject = new JsonObject();
                        relationshipArrayObject.add(relationship.getName(), namedRelationshipInstancesArray);
                        relationshipsArray.add(relationshipArrayObject);

                        hasAnyComplexRelationships = true;
                    }
                }
            }

            if(hasAnyComplexRelationships) {
                jsonobj.add("relationships", relationshipsArray);
            }
        }

        return jsonobj;
    }

    /**
     * This is more suitable for XML output
     * @param things
     * @param defn
     * @return
     */
    public String asJsonTypedArrayWithContentsTyped(final List<ThingInstance> things, ThingDefinition defn) {

        final JsonObject arrayObj = new JsonObject();
        arrayObj.add(defn.getPlural(), asJsonArrayInstanceWrapped(things));
        return arrayObj.toString();
    }


    /**
     * This is suitable for partial XML output but should never be used directly as it needs a wrapper name to make sense
     * @param things
     * @return
     */
    private JsonArray asJsonArrayInstanceWrapped(Collection<ThingInstance> things) {


        // [{"item":{"guid":"bob"}}, {"item":{"guid":"bob2"}}]

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
    public JsonObject asNamedJsonObject(final ThingInstance instance) {

        final JsonObject retObj = new JsonObject();
        retObj.add(instance.getEntity().getName(), asJsonObject(instance));
        return retObj;

    }

}
