package uk.co.compendiumdev.thingifier.adapter.httpserver;

public final class HaltRequestException extends RuntimeException {
    private final int statusCode;
    private final String body;

    public HaltRequestException(final int statusCode, final String body) {
        super(body);
        this.statusCode = statusCode;
        this.body = body == null ? "" : body;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }
}
