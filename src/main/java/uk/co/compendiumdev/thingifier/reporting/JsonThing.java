package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {


    // TODO this seems like overkill - will we ever return multiple types of things?
    public static String asJson(final List<ThingInstance> things) {

        if (things == null || things.size() == 0) {
            return "{}";
        }

        Map<String, Set<ThingInstance>> sets = new HashMap<>();

        // collate the names of the things
        for (ThingInstance thing : things) {
            String nameOfThings = thing.getEntity().getPlural();
            Set<ThingInstance> setOf = sets.get(nameOfThings);
            if (setOf == null) {
                setOf = new HashSet<>();
                sets.put(nameOfThings, setOf);
            }
            setOf.add(thing);
        }


        // if there are multiple sets then

        // {things : [  {things1 : []} , {things2 : []} ]
        // if there is a single set then
        // {things1 : []}

        final JsonArray jsonArray = new JsonArray();
        JsonObject arrayObj = null;

        Set<String> keys = sets.keySet();

        for (String typeName : keys) {

            arrayObj = new JsonObject();
            arrayObj.add(typeName, asJsonArray(sets.get(typeName)));
            jsonArray.add(arrayObj);

        }

        if (sets.size() > 1) {

            JsonObject returnObject = new JsonObject();
            returnObject.add("things", jsonArray);
            return returnObject.toString();

        } else{

            return arrayObj.toString();
        }

    }

    public static JsonArray asJsonArray(final Collection<ThingInstance> things) {

        // [{"guid":"bob"}, {"guid":"bob2"}]

        final JsonArray jsonArray = new JsonArray();

        for (ThingInstance thing : things) {
            jsonArray.add(asJsonObject(thing));

        }

        System.out.println(jsonArray.toString());
        return jsonArray;
    }

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
