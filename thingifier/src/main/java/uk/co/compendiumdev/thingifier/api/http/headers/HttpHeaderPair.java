package uk.co.compendiumdev.thingifier.api.http.headers;

public final class HttpHeaderPair {

    public final String key;
    public final String value;

    public HttpHeaderPair(final String key, final String value) {
        this.key = key == null ? "" : key;
        this.value = value == null ? "" : value;
    }
}
