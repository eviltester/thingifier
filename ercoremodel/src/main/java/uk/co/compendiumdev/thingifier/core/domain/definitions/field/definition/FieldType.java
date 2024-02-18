package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

public enum FieldType {

    FLOAT("0.0"),
    AUTO_INCREMENT("1"),
    INTEGER("0"),
    OBJECT(null),
    STRING(""),


    DATE(null), // todo
    BOOLEAN("false"),
    ENUM(""), // remember to set example values
    //TODO: add an AUTO_GUID field type
    GUID(null);


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

