package uk.co.compendiumdev.thingifier.api.http;

import java.util.HashMap;
import java.util.Map;

public class HttpApiRequest {
    private final String path;
    private Map<String, String> headers;

    public HttpApiRequest(final String pathInfo) {
        this.path = pathInfo;
        this.headers = new HashMap<>();
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
}
