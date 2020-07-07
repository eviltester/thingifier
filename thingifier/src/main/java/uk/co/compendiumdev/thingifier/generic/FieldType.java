package uk.co.compendiumdev.thingifier.generic;

public enum FieldType {

    STRING(""),
    INTEGER("0"),
    DATE(null),
    BOOLEAN("FALSE"),
    ENUM(""); // remember to set example values

    private final String defaultValue;

    FieldType(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }
}

// TODO add type for SET having a set of valid values
// TODO add type for EXPANDINGSET having a set of valid initial values, but allow user expansion
// TODO add type for ID having an incremental unique id per entity type
// TODO add type for CREATEDDATE - automatically maintained - possibly allow configuring format
// TODO add type for AMENDEDDATE - automatically maintained - possibly allow configuring format
