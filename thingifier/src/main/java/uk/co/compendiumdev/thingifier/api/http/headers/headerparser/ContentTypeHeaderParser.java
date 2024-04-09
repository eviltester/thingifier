package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

public class ContentTypeHeaderParser {
    private final String header;

    public ContentTypeHeaderParser(final String header) {
        if(header==null){
            this.header="";
        }else {
            this.header = header.trim().toLowerCase();
        }
    }

    public boolean isXML() {
        // text/xml in standard https://datatracker.ietf.org/doc/html/rfc3023
        return header.contains("application/xml") || header.contains("text/xml");
    }

    public boolean isJSON() {
        return header.contains("application/json");
    }

    public boolean isMissing() {
        return (header.isEmpty());
    }

    public boolean isText() {
        return header.contains("text/");
    }
}
