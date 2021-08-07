package uk.co.compendiumdev.thingifier.api.http;

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
        return header.contains("application/xml");
    }

    public boolean isJSON() {
        return header.contains("application/json");
    }

    public boolean isMissing() {
        return (header.length()==0);
    }

    public boolean isText() {
        return header.contains("text/");
    }
}
