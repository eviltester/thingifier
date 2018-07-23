package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {



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



    private static JsonObject asJsonObject(final ThingInstance thingInstance) {

        final JsonObject jsonobj = new JsonObject();

        if (thingInstance == null) {
            return jsonobj;
        }


        for (String field : thingInstance.getEntity().getFieldNames()) {

            jsonobj.addProperty(field, thingInstance.getValue(field));
        }


        return jsonobj;


    }

    public static JsonObject asNamedJsonObject(final ThingInstance instance) {

        final JsonObject retObj = new JsonObject();
        retObj.add(instance.getEntity().getName(), JsonThing.asJsonObject(instance));
        return retObj;

    }
}
