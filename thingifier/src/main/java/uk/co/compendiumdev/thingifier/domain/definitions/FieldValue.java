package uk.co.compendiumdev.thingifier.domain.definitions;

import java.util.HashMap;
import java.util.Map;

final public class FieldValue {

    private final String fieldName; // should this be name or should it be a Field reference?
    private final String fieldValue;
    // todo: InstanceFields for an object
    // todo: list of strings for an array
    // todo: list of InstanceFields for an array of objects

    private FieldValue(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public String toString() {
        return "FieldValue{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                '}';
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

    public FieldValue cloned() {
        final FieldValue clone = new FieldValue(fieldName, fieldValue);
        return clone;
    }
}
