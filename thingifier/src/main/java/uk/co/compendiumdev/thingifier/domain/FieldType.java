package uk.co.compendiumdev.thingifier.domain;

public enum FieldType {

    STRING(""),
    INTEGER("0"),
    FLOAT("0.0"),
    DATE(null), // todo
    BOOLEAN("FALSE"),
    ENUM(""), // remember to set example values
    ID(null), // TODO: validate against more than one against a thing
    GUID(null), // TODO: validate against more than one against a thing
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
// TODO: ARRAY to contain an array of Entity Instances
// TODO: OBJECT to contain an instance of a specific Entity Type
// TODO add type for CREATEDDATE - automatically maintained - possibly allow configuring format
// TODO add type for AMENDEDDATE - automatically maintained - possibly allow configuring format

