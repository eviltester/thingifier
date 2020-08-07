package uk.co.compendiumdev.thingifier.domain;

public enum FieldType {

    STRING(""),
    INTEGER("0"),
    FLOAT("0.0"),
    DATE(null), // todo
    BOOLEAN("false"),
    ENUM(""), // remember to set example values
    ID(null),
    GUID(null),
    OBJECT(null); // essentially a hashmap of other fields

    private final String defaultValue;

    FieldType(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }
}

// TODO: TIME field type
// TODO: DATE_TIME field type
// TODO: ARRAY to contain an array of other fields
// TODO add type for CREATEDDATE - automatically maintained - possibly allow configuring format
// TODO add type for AMENDEDDATE - automatically maintained - possibly allow configuring format

