package uk.co.compendiumdev.thingifier.adapter.httpserver;

@FunctionalInterface
public interface HttpRouteHandler {
    String handle(HttpServerRequest request, HttpServerResponse response) throws Exception;
}
