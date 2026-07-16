package uk.co.compendiumdev.thingifier.adapter.httpserver.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;

public final class HttpServerRequestToInternalHttpRequest {

    private HttpServerRequestToInternalHttpRequest() {}

    public static InternalHttpRequest convert(final HttpServerRequest request) {
        InternalHttpRequest internalRequest =
                new InternalHttpRequest(request.pathInfo())
                        .setBody(request.body())
                        .setQueryParams(queryParamsAsMap(request))
                        .setRawQueryString(request.queryString())
                        .setMethod(request.method())
                        .setUrl(request.url())
                        .setIP(request.ip())
                        .setUrlParams(request.urlParams());

        addRawHeaders(internalRequest, request);

        return internalRequest;
    }

    private static void addRawHeaders(
            final InternalHttpRequest internalRequest, final HttpServerRequest request) {

        for (String headerName : request.headerNames()) {
            internalRequest.addHeader(headerName, request.header(headerName));
        }
    }

    private static Map<String, List<String>> queryParamsAsMap(final HttpServerRequest request) {
        return new LinkedHashMap<>(request.queryParamMap());
    }
}
