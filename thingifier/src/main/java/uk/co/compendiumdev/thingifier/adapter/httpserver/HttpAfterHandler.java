package uk.co.compendiumdev.thingifier.adapter.httpserver;

@FunctionalInterface
public interface HttpAfterHandler {
    void handle(HttpServerRequest request, HttpServerResponse response) throws Exception;
}
