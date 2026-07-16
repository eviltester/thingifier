package uk.co.compendiumdev.thingifier.adapter.javalin;

import io.javalin.http.Context;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

final class JavalinServerResponse implements HttpServerResponse {
    static final String RESPONSE_BODY_ATTRIBUTE = "thingifier.response.body";

    private final Context context;
    private boolean bodySet;

    JavalinServerResponse(final Context context) {
        this.context = context;
    }

    @Override
    public String body() {
        String body = context.attribute(RESPONSE_BODY_ATTRIBUTE);
        return body == null ? context.result() : body;
    }

    @Override
    public void body(final String body) {
        bodySet = true;
        context.attribute(RESPONSE_BODY_ATTRIBUTE, body == null ? "" : body);
        context.result((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean containsHeader(final String name) {
        return context.res().containsHeader(name);
    }

    @Override
    public void header(final String name, final String value) {
        context.header(name, value);
    }

    @Override
    public Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String name : context.res().getHeaderNames()) {
            headers.put(name, context.res().getHeader(name));
        }
        return headers;
    }

    @Override
    public void redirect(final String location) {
        context.redirect(location);
    }

    @Override
    public void redirect(final String location, final int statusCode) {
        context.status(statusCode);
        context.header("Location", location);
    }

    @Override
    public int status() {
        return context.statusCode();
    }

    @Override
    public void status(final int statusCode) {
        context.status(statusCode);
    }

    @Override
    public String type() {
        return context.header("Content-Type");
    }

    @Override
    public void type(final String contentType) {
        context.header("Content-Type", contentType);
    }

    boolean wasBodySet() {
        return bodySet;
    }
}
