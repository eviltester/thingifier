package uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public final class HttpApiResponseToInternalHttpResponse {

    private HttpApiResponseToInternalHttpResponse() {}

    public static InternalHttpResponse convert(final HttpApiResponse httpResponse) {
        InternalHttpResponse internalResponse =
                new InternalHttpResponse()
                        .setStatus(httpResponse.getStatusCode())
                        .setBody(httpResponse.getBody());

        if (httpResponse.hasType()) {
            internalResponse.setType(httpResponse.getType());
        }

        for (Map.Entry<String, String> header : httpResponse.getHeaders().asMap().entrySet()) {
            internalResponse.setHeader(header.getKey(), header.getValue());
        }

        return internalResponse;
    }
}
