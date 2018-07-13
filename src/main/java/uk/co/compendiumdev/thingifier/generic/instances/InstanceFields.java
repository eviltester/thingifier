package uk.co.compendiumdev.thingifier.generic.instances;

import java.util.*;

public class InstanceFields {

    private Map<String,String> values = new HashMap<String,String>();
    private String[] fieldValuesFrom;

    public void addValue(String fieldName, String value){
        values.put(fieldName.toLowerCase(), value);
    }

    public String getValue(String fieldName){
        return values.get(fieldName.toLowerCase());
    }

    public List<String> getFields(){
        List<String> fields = new ArrayList<String>();
        fields.addAll(values.keySet());
        return fields;
    }

    public String toString(){

        StringBuilder output = new StringBuilder();

        for(Map.Entry<String, String> entry : values.entrySet()){
            output.append("\n\t\t\t\t" + entry.getKey() + " : " + entry.getValue() + "\n" );
        }

        return output.toString();
    }

    public InstanceFields setFieldValuesFrom(String[] fieldValuesFrom) {

        //todo: add validation in here, this is naive to start us off
        for(String aFieldValuePair : fieldValuesFrom){
            String[] pairValues = aFieldValuePair.split(":");
            addValue(pairValues[0], pairValues[1]);
        }

        return this;
    }

    public void deleteAllFieldsExcept(String... fieldNamesToIgnore) {

        Set<String> ignorekeys = new HashSet<>(Arrays.asList(fieldNamesToIgnore));

        for(String key : values.keySet()){
            if(!ignorekeys.contains(key)) {
                values.remove(key);
            }
        }
    }
}
