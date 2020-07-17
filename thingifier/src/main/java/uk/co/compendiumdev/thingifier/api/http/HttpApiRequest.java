package uk.co.compendiumdev.thingifier.api.http;

import java.util.HashMap;
import java.util.Map;

public final class HttpApiRequest {

    private final String path;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> queryParams;
    private VERB verb;

    public enum VERB{ GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, CONNECT, TRACE}

    public HttpApiRequest(final String pathInfo) {
        this.path = justThePath(pathInfo);
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        body = "";
        verb = VERB.GET;
    }

    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public HttpApiRequest setHeaders(final Map<String, String> mapOfHeaderValues) {
        this.headers.putAll(mapOfHeaderValues);
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public HttpApiRequest setBody(final String theBody) {
        this.body = theBody;
        return this;
    }

    public String getBody() {
        return this.body;
    }

    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }

    public void addHeader(final String headerName, final String headerValue) {
        this.headers.put(headerName, headerValue);
    }

    public HttpApiRequest setQueryParams(final Map<String, String> queryParamsAsMap) {
        queryParams = queryParamsAsMap;
        return this;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getHeader(final String headerName, final String aDefault) {
        String header = getHeader(headerName);
        if(header==null){
            header=aDefault;
        }
        return header;
    }

    public HttpApiRequest setVerb(final VERB verb) {
        this.verb = verb;
        return this;
    }

    public VERB getVerb() {
        return this.verb;
    }
}
