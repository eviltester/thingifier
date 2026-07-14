package uk.co.compendiumdev.thingifier.api.http.bodyparser;

public final class ApiBodyField {

    private final String name;
    private final String value;
    private final String sourceType;

    public ApiBodyField(final String name, final String value) {
        this(name, value, "STRING");
    }

    public ApiBodyField(final String name, final String value, final String sourceType) {
        this.name = name;
        this.value = value;
        this.sourceType = sourceType == null ? "Something Else" : sourceType;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String sourceType() {
        return sourceType;
    }
}
