package uk.co.compendiumdev.thingifier.api;

public class ResponseHeader {
    public final String headerName;
    public final String headerValue;

    public ResponseHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }
}
