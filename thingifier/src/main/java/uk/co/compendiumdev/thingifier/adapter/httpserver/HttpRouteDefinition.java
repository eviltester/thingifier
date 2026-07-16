package uk.co.compendiumdev.thingifier.adapter.httpserver;

public final class HttpRouteDefinition {
    private final HttpRouteVerb verb;
    private final String path;
    private final HttpRouteHandler handler;

    HttpRouteDefinition(
            final HttpRouteVerb verb, final String path, final HttpRouteHandler handler) {
        this.verb = verb;
        this.path = path;
        this.handler = handler;
    }

    public HttpRouteVerb verb() {
        return verb;
    }

    public String path() {
        return path;
    }

    public HttpRouteHandler handler() {
        return handler;
    }
}
