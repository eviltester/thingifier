package uk.co.compendiumdev.thingifier.generic;

public enum FieldType {

    STRING(""),
    INTEGER("0"),
    FLOAT("0.0"),
    DATE(null), // todo
    BOOLEAN("FALSE"),
    ENUM(""), // remember to set example values
    ID(null);

    private final String defaultValue;

    FieldType(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }
}

// TODO add type for ID having an incremental unique id per entity type
// TODO: TIME field type
// TODO: DATE_TIME field type
// TODO: ARRAY to contain an array of Entity Instances
// TODO: OBJECT to contain an instance of a specific Entity Type
// TODO add type for CREATEDDATE - automatically maintained - possibly allow configuring format
// TODO add type for AMENDEDDATE - automatically maintained - possibly allow configuring format
// TODO: have an 'overrideValue' method which ignores all validation performed by setValue and can be used for internal use or migration routines
