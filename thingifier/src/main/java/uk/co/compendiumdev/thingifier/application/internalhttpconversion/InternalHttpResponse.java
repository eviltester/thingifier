package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import java.util.HashMap;
import java.util.Map;

/*
    The HttpApiResponse is too complicated to re-use and is tied to the ApiResponse

    This is just a cleaner bridge for pure Http access.

 */
public class InternalHttpResponse {
    private int status;
    private String contentType;
    private String body;
    private Map<String, String> headers;

    public InternalHttpResponse(){

        headers = new HashMap<>();
    }

    public InternalHttpResponse setStatus(final int status) {
        this.status = status;
        return this;
    }

    public InternalHttpResponse setType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public InternalHttpResponse setBody(final String body) {
        this.body = body;
        return this;
    }

    public InternalHttpResponse setHeader(final String headerName, final String header) {
        headers.put(headerName.toUpperCase(), header);
        return this;
    }

    public int getStatusCode() {
        return status;
    }

    public boolean hasType() {
        return contentType!=null;
    }

    public String getType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName.toUpperCase());
    }
}
