package uk.co.compendiumdev.thingifier.crudui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UiHttpResponse {

    private final int statusCode;
    private final String contentType;
    private final String body;
    private final Map<String, String> headers;

    public UiHttpResponse(final int statusCode, final String contentType, final String body) {
        this(statusCode, contentType, body, new LinkedHashMap<>());
    }

    public UiHttpResponse(
            final int statusCode,
            final String contentType,
            final String body,
            final Map<String, String> headers) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body == null ? "" : body;
        this.headers = new LinkedHashMap<>(headers);
    }

    public static UiHttpResponse json(final int statusCode, final String body) {
        return new UiHttpResponse(statusCode, "application/json", body);
    }

    public static UiHttpResponse html(final String body) {
        return new UiHttpResponse(200, "text/html", body);
    }

    public int statusCode() {
        return statusCode;
    }

    public String contentType() {
        return contentType;
    }

    public String body() {
        return body;
    }

    public Map<String, String> headers() {
        return new LinkedHashMap<>(headers);
    }
}
