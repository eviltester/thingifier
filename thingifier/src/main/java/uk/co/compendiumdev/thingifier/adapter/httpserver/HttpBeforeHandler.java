package uk.co.compendiumdev.thingifier.adapter.httpserver;

@FunctionalInterface
public interface HttpBeforeHandler {
    void handle(HttpServerRequest request, HttpServerResponse response) throws Exception;
}
