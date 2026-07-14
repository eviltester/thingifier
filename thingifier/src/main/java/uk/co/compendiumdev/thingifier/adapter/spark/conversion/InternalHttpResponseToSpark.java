package uk.co.compendiumdev.thingifier.adapter.spark.conversion;

import java.util.Map;
import spark.Response;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public final class InternalHttpResponseToSpark {

    private InternalHttpResponseToSpark() {}

    public static String convert(
            final InternalHttpResponse internalResponse, final Response response) {
        updateResponseFromInternal(response, internalResponse);
        return internalResponse.getBody();
    }

    public static void updateResponseFromInternal(
            final Response response, final InternalHttpResponse httpResponse) {

        response.status(httpResponse.getStatusCode());

        if (httpResponse.hasType()) {
            response.type(httpResponse.getType());
        }

        for (Map.Entry<String, String> header : httpResponse.getHeaders().asMap().entrySet()) {
            response.raw().setHeader(header.getKey(), header.getValue());
        }

        response.body(httpResponse.getBody());
    }
}
