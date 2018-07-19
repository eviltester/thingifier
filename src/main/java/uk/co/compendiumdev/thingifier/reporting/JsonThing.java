package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {

    public static String asJsonObjectWithWrapperObject(ThingInstance thingInstance) {

        if(thingInstance==null){
            return "{}";
        }

        return asJsonObjectWithNamedWrapperObject(thingInstance, thingInstance.getEntity().getName());
    }

    public static String asJsonObjectWithNamedWrapperObject(ThingInstance thingInstance, String name) {

        if(thingInstance==null){
            return "{}";
        }

        StringBuilder json = new StringBuilder();

        json.append(String.format("{ \"%s\" : %s }",
                name,
                asJson(thingInstance)));

        return json.toString();
    }

    public static String jsonObjectWrapper(String objectName, String json){

        return String.format("{ \"%s\" : %s }",
                objectName,
                json);

    }



    // TODO this seems like overkill - will we ever return multiple types of things?
    public static String asJson(List<ThingInstance> things) {

        if(things==null || things.size()==0){
            return "{}";
        }

        Map<String, Set<ThingInstance>> sets = new HashMap<>();

        // collate the names of the things
        for(ThingInstance thing : things){
            String nameOfThings = thing.getEntity().getPlural();
            Set<ThingInstance> setOf = sets.get(nameOfThings);
            if(setOf==null){
                setOf = new HashSet<>();
                sets.put(nameOfThings, setOf);
            }
            setOf.add(thing);
        }


        // if there are multiple sets then

        // {things : [  {things1 : []} , {things2 : []} ]
        // if there is a single set then
        // {things1 : []}

        StringBuilder json = new StringBuilder();

        // output
        String prepend = "";

        if(sets.size()>1){
            json.append("{\"things\" : [ ");
        }

        for(String typeName : sets.keySet()){

            json.append(prepend);
            json.append(jsonObjectWrapper(typeName, asJsonArray(sets.get(typeName))));
            prepend = ", ";
        }

        if(sets.size()>1){
            json.append("]}");
        }



        return json.toString();
    }

    public static String asJsonArray(Collection<ThingInstance> things) {

        StringBuilder json = new StringBuilder();

        json.append("[");

        String arrayPrepend = "";
        for(ThingInstance thing : things){

            json.append(String.format("%s %s", arrayPrepend, asJson(thing)));
            arrayPrepend = ", ";
        }

        json.append("]");

        return json.toString();
    }

    public static String asJsonArrayInstanceWrapped(Collection<ThingInstance> things, String wrapperName) {

        StringBuilder json = new StringBuilder();

        json.append("[");

        String arrayPrepend = "";
        for(ThingInstance thing : things){

            json.append(String.format("%s {\"%s\" : %s}", arrayPrepend, wrapperName, asJson(thing)));
            arrayPrepend = ", ";
        }

        json.append("]");

        return json.toString();
    }

    public static String asJson(ThingInstance thingInstance) {

        if(thingInstance==null){
            return "{}";
        }

        StringBuilder json = new StringBuilder();

        json.append(String.format("{ %s }",
                getFieldsAsJson(thingInstance)));

        return json.toString();
    }


    private static String getFieldsAsJson(ThingInstance thing) {
        StringBuilder json = new StringBuilder();
        String prepend = "";

        for(String field : thing.getEntity().getFieldNames()){

            json.append(String.format("%s \"%s\" : \"%s\"", prepend, field, thing.getValue(field)));
            prepend=", ";
        }

        return json.toString();
    }


}
