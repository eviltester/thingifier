package uk.co.compendiumdev.thingifier.domain.definitions.field.instance;

import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;

final public class FieldValue {

    private final String fieldName; // should this be name or should it be a Field reference?
    private final String fieldValue;
    private InstanceFields objectValue;
    // todo: list of strings for an array
    // todo: list of InstanceFields for an array of objects

    private FieldValue(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.objectValue = null;
    }

    @Override
    public String toString() {
        String string =  "FieldValue{" +
                "fieldName='" + fieldName + "'" +
                ", fieldValue='" + fieldValue + "'";
        if(objectValue!=null){
            string = string + ",{ " + objectValue.toString() + " }";
        }
        string = string + "}";

        return string;
    }

    public static FieldValue is(String fieldName, String fieldValue) {
        return new FieldValue(fieldName, fieldValue);
    }

    public static FieldValue is(String fieldName, InstanceFields objectValue) {
        final FieldValue value = new FieldValue(fieldName, "");
        value.setValue(objectValue);
        return value;
    }

    private FieldValue setValue(final InstanceFields objectValue) {
        this.objectValue = objectValue;
        return this;
    }

    public String getName() {
        return fieldName;
    }


    public FieldValue cloned() {
        if(objectValue!=null){
            return FieldValue.is(fieldName, objectValue.cloned());
        }else{
            return FieldValue.is(fieldName, fieldValue);
        }
    }

    public String asString() {
        return fieldValue;
    }

    public InstanceFields asObject() {
        return objectValue;
    }

    public float asFloat() {
        return Float.valueOf(fieldValue);
    }

    public boolean asBoolean() {
        if (fieldValue.toLowerCase().contentEquals("true")){
            return true;
        }
        if(fieldValue.toLowerCase().contentEquals("false")) {
            return false;
        }

        throw new IllegalArgumentException(fieldValue + " is not boolean");
    }

    public int asInteger() {
        return Integer.valueOf(fieldValue);
    }
}
