package uk.co.compendiumdev.thingifier.application.internalhttp;

public final class InternalHttpHeader {

    private final String key;
    private final String value;

    public InternalHttpHeader(final String key, final String value) {
        this.key = key == null ? "" : key;
        this.value = value == null ? "" : value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }
}
