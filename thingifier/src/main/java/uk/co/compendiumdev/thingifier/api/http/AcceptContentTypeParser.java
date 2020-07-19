package uk.co.compendiumdev.thingifier.api.http;

public class AcceptContentTypeParser {
    private final String header;

    public AcceptContentTypeParser(final String header) {
        this.header = header;
    }

    public boolean isXML() {
        return header.trim().contains("application/xml");
    }

    public boolean isJSON() {
        return header.trim().contains("application/json");
    }

}
