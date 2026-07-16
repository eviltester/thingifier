package uk.co.compendiumdev.thingifier.adapter.httpserver.conversion;

import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public class HttpServerResponseToInternalHttpResponse {

    public static InternalHttpResponse convert(HttpServerResponse response) {
        InternalHttpResponse internalResponse = new InternalHttpResponse();

        internalResponse.setStatus(response.status());
        internalResponse.setType(response.type()); // content type
        internalResponse.setBody(response.body());

        for (String headerName : response.headers().keySet()) {
            internalResponse.setHeader(headerName, response.headers().get(headerName));
        }

        return internalResponse;
    }

    public static void updateResponseFromInternal(
            final HttpServerResponse response, final InternalHttpResponse httpResponse) {
        InternalHttpResponseToHttpServer.updateResponseFromInternal(response, httpResponse);
    }
}
