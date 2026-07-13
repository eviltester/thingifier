package uk.co.compendiumdev.thingifier.application.internalhttp;

public final class InternalHttpResponse {

    private int status;
    private String contentType;
    private String body;
    private final InternalHttpHeaders headers;

    public InternalHttpResponse() {
        headers = new InternalHttpHeaders();
        body = "";
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
        this.body = body == null ? "" : body;
        return this;
    }

    public InternalHttpResponse setHeader(final String headerName, final String header) {
        headers.put(headerName, header);
        return this;
    }

    public int getStatusCode() {
        return status;
    }

    public boolean hasType() {
        return contentType != null;
    }

    public String getType() {
        return contentType;
    }

    public InternalHttpHeaders getHeaders() {
        InternalHttpHeaders copy = new InternalHttpHeaders();
        copy.putAll(headers);
        return copy;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }

    public boolean hasHeader(final String headerName) {
        return headers.headerExists(headerName);
    }
}
