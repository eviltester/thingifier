package uk.co.compendiumdev.thingifier.generic.definitions;

final public class FieldValue {

    private final String fieldName;
    private final String fieldValue;

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
}
