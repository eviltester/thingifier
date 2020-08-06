package uk.co.compendiumdev.thingifier.domain.definitions;

import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;

import java.util.HashMap;
import java.util.Map;

final public class FieldValue {

    private final String fieldName; // should this be name or should it be a Field reference?
    private final String fieldValue;
    private InstanceFields objectValue;
    // todo: InstanceFields for an object
    // todo: list of strings for an array
    // todo: list of InstanceFields for an array of objects

    private FieldValue(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.objectValue = null;
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

    /**
     *
     * @deprecated We should be using asString, asInteger, asObject, asArray style methods
     */
    @Deprecated
    public String getValue() {
        return fieldValue;
    }

    public FieldValue cloned() {
        final FieldValue clone = new FieldValue(fieldName, fieldValue);
        return clone;
    }

    public String asString() {
        return fieldValue;
    }
}
