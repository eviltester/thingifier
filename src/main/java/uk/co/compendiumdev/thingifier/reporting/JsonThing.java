package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class JsonThing {

    public static String asJson(ThingInstance thingInstance) {

        StringBuilder json = new StringBuilder();

        json.append(String.format("{ \"%s\" : { %s }}",
                                    thingInstance.getEntity().getName(),
                                    getFieldsAsJson(thingInstance)));

        return json.toString();
    }

    public static String asJson(List<ThingInstance> things) {
        Map<String, Set<ThingInstance>> sets = new HashMap<>();

        // collate
        for(ThingInstance thing : things){
            Set<ThingInstance> setOf = sets.get(thing.getEntity().getName());
            if(setOf==null){
                setOf = new HashSet<>();
                sets.put(thing.getEntity().getName(), setOf);
            }
            setOf.add(thing);
        }

        StringBuilder json = new StringBuilder();

        json.append("{");
        // output
        String prepend = "";
        boolean firstInSet = true;

        for(Set<ThingInstance> set : sets.values()){

            firstInSet = true;

            String arrayPrepend = "";

            for(ThingInstance thing : set){

                if(firstInSet){
                    json.append(String.format("%s \"%s\" : [", prepend, thing.getEntity().getPlural()));
                    prepend=", ";
                    firstInSet=false;
                }

                json.append(String.format("%s {%s}", arrayPrepend, getFieldsAsJson(thing)));
                arrayPrepend = ", ";
            }

            json.append("]");


        }
        json.append("}");

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
