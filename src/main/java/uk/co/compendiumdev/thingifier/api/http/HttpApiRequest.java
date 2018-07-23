package uk.co.compendiumdev.thingifier.api.http;

import java.util.HashMap;
import java.util.Map;

public class HttpApiRequest {
    private final String path;
    private Map<String, String> headers;
    private boolean hasBody;
    private String body;

    public HttpApiRequest(final String pathInfo) {
        this.path = justThePath(pathInfo);
        this.headers = new HashMap<>();
        hasBody=false;
        body="";
    }

    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public HttpApiRequest setHeaders(final Map<String, String> mapOfHeaderValues) {
        this.headers = mapOfHeaderValues;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public HttpApiRequest setBody(final String theBody) {
        this.hasBody = true;
        this.body = theBody;
        return this;
    }

    public String getBody() {
        return this.body;
    }

    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }
}
