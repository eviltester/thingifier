package uk.co.compendiumdev.thingifier.generic;

public enum FieldType {

    STRING(""), INTEGER(""), DATE(null), BOOLEAN("FALSE");

    private final String defaultValue;

    FieldType(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }
}

// TODO add type for SET having a set of valid values
