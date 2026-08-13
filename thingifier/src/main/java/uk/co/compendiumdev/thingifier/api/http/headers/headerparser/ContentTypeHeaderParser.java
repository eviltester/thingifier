package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

public class ContentTypeHeaderParser {
    private final String header;

    public ContentTypeHeaderParser(final String header) {
        if (header == null) {
            this.header = "";
        } else {
            this.header = header.trim().toLowerCase();
        }
    }

    public boolean isXML() {
        // text/xml in standard https://datatracker.ietf.org/doc/html/rfc3023
        return isMediaType("application/xml") || isMediaType("text/xml");
    }

    public boolean isJSON() {
        return isMediaType("application/json");
    }

    public boolean isJsonMergePatch() {
        return isMediaType("application/merge-patch+json");
    }

    public boolean isJsonPatch() {
        return isMediaType("application/json-patch+json");
    }

    public boolean isFormUrlEncoded() {
        return isMediaType("application/x-www-form-urlencoded");
    }

    public boolean isJsonPath() {
        return isMediaType("application/jsonpath");
    }

    public boolean isStructuredQueryJson() {
        return isMediaType("application/vnd.thingifier.query+json");
    }

    public boolean isMissing() {
        return (header.isEmpty());
    }

    public boolean isText() {
        return mediaType().startsWith("text/");
    }

    public boolean isMediaType(final String mediaType) {
        return mediaType().equalsIgnoreCase(mediaType);
    }

    public String mediaType() {
        int separatorIndex = header.indexOf(";");
        if (separatorIndex == -1) {
            return header;
        }
        return header.substring(0, separatorIndex).trim();
    }
}
