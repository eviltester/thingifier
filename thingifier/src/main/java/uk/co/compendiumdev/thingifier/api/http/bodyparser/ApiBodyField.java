package uk.co.compendiumdev.thingifier.api.http.bodyparser;

public final class ApiBodyField {

    private final String name;
    private final String value;

    public ApiBodyField(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }
}
