package uk.co.compendiumdev.thingifier.domain.definitions;

import java.util.HashMap;
import java.util.Map;

final public class FieldValue {

    private final String fieldName;
    private final String fieldValue;
    private Map<String, FieldValue> fieldValues;

    public FieldValue(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public static FieldValue is(String fieldName, String fieldValue) {
        return new FieldValue(fieldName, fieldValue);
    }

    public String getName() {
        return fieldName;
    }

    public String getValue() {
        return fieldValue;
    }

    public Map<String, FieldValue> getValues() {
        return fieldValues;
    }

    public void addObjectField(String fieldName, String fieldValue){

        if(fieldValues==null){
            this.fieldValues = new HashMap<>();
        }

        fieldValues.put(fieldName, new FieldValue(fieldName, fieldValue));
    }
}
