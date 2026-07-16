package uk.co.compendiumdev.thingifier.adapter.httpserver.conversion;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public final class InternalHttpResponseToHttpServer {

    private InternalHttpResponseToHttpServer() {}

    public static String convert(
            final InternalHttpResponse internalResponse, final HttpServerResponse response) {
        updateResponseFromInternal(response, internalResponse);
        return internalResponse.getBody();
    }

    public static void updateResponseFromInternal(
            final HttpServerResponse response, final InternalHttpResponse httpResponse) {

        response.status(httpResponse.getStatusCode());

        if (httpResponse.hasType()) {
            response.type(httpResponse.getType());
        }

        response.body(httpResponse.getBody());

        for (Map.Entry<String, String> header : httpResponse.getHeaders().asMap().entrySet()) {
            response.header(header.getKey(), header.getValue());
        }
    }
}
