package uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks;

import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

/**
 * HttpRequestResponseHook (s) are run prior to any processing. They work directly on the httpserver
 * request and httpserver responses
 */
public interface HttpRequestResponseHook {
    // throw an exception if we want to 'stop' the request and return the response
    void run(HttpServerRequest request, HttpServerResponse response);
}
