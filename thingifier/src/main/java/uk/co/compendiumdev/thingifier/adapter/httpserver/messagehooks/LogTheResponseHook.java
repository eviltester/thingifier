package uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks;

import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public class LogTheResponseHook implements HttpRequestResponseHook {
    @Override
    public void run(final HttpServerRequest request, final HttpServerResponse response) {
        try {
            System.out.println("**RESPONSE**");
            System.out.println(response.status());
            System.out.println(response.body());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
