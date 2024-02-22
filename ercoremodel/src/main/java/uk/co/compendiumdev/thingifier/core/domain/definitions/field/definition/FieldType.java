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

    AUTO_GUID(null);

    // TODO: add a GUID type back in which validates against a GUID, this field type is editable

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

