package uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks;

import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public class LogTheHttpRequestHook implements HttpRequestResponseHook {
    @Override
    public void run(final HttpServerRequest request, final HttpServerResponse response) {
        try {

            System.out.println("**REQUEST**");
            System.out.println(request.url());
            System.out.println(request.pathInfo());
            System.out.println(request.body());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
