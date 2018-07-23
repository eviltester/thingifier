package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {


//    // TODO this seems like overkill - will we ever return multiple types of things?
//    public static String asJson(final List<ThingInstance> things) {
//
//        if (things == null || things.size() == 0) {
//            return "{}";
//        }
//
//        Map<String, Set<ThingInstance>> sets = new HashMap<>();
//
//        // collate the names of the things
//        for (ThingInstance thing : things) {
//            String nameOfThings = thing.getEntity().getPlural();
//            Set<ThingInstance> setOf = sets.get(nameOfThings);
//            if (setOf == null) {
//                setOf = new HashSet<>();
//                sets.put(nameOfThings, setOf);
//            }
//            setOf.add(thing);
//        }
//
//
//        // if there are multiple sets then
//
//        // {things : [  {things1 : []} , {things2 : []} ]
//        // if there is a single set then
//        // {things1 : []}
//
//        final JsonArray jsonArray = new JsonArray();
//        JsonObject arrayObj = null;
//
//        Set<String> keys = sets.keySet();
//
//        for (String typeName : keys) {
//
//            arrayObj = new JsonObject();
//            arrayObj.add(typeName, asJsonArray(sets.get(typeName)));
//            jsonArray.add(arrayObj);
//
//        }
//
//        if (sets.size() > 1) {
//
//            JsonObject returnObject = new JsonObject();
//            returnObject.add("things", jsonArray);
//            return returnObject.toString();
//
//        } else{
//
//            return arrayObj.toString();
//        }
//    }

    /**
     * Deprecated, should really use asJson(things, defn) to create arrays with correct plural and type based on definition
     *
     * or use `asJson(things, typeName)` instead so that the array always has a plural in front of it
     *
     * @param things
     * @return
     */
    @Deprecated
    public static String asJson(final List<ThingInstance> things) {
        return asJsonArray(things).toString();
    }

    /**
     * This is more suitable for JSON output
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
     * This is suitable for JSON output
     * @param things
     * @return
     */
    public static JsonArray asJsonArray(final Collection<ThingInstance> things) {

        // [{"guid":"bob"}, {"guid":"bob2"}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {
            jsonArray.add(asJsonObject(thing));

        }

        System.out.println(jsonArray.toString());
        return jsonArray;
    }


    /**
     * This is suitable for XML output
     * @param things
     * @return
     */
    public static JsonArray asJsonArrayInstanceWrapped(Collection<ThingInstance> things) {


        // [{"todo":{"guid":"bob"}}, {"todo":{"guid":"bob2"}}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {

            JsonObject jsonObj = new JsonObject();
            jsonObj.add(thing.getEntity().getName(), asJsonObject(thing));
            jsonArray.add(jsonObj);

        }

        System.out.println(jsonArray.toString());
        return jsonArray;
    }

    /**
     * Should use the asJsonArrayInstanceWrapped(things) to automatically add correct typeNames in the array objects
     * @param things
     * @param wrapperName
     * @return
     */
    @Deprecated
    public static JsonArray asJsonArrayInstanceWrapped(Collection<ThingInstance> things, String wrapperName) {


        // [{"todo":{"guid":"bob"}}, {"todo":{"guid":"bob2"}}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {

            JsonObject jsonObj = new JsonObject();
            jsonObj.add(wrapperName, asJsonObject(thing));
            jsonArray.add(jsonObj);

        }

        System.out.println(jsonArray.toString());
        return jsonArray;
    }

    public static JsonObject asJsonObject(final ThingInstance thingInstance) {

        final JsonObject jsonobj = new JsonObject();

        if (thingInstance == null) {
            return jsonobj;
        }


        for (String field : thingInstance.getEntity().getFieldNames()) {

            jsonobj.addProperty(field, thingInstance.getValue(field));
        }


        return jsonobj;


    }

}
