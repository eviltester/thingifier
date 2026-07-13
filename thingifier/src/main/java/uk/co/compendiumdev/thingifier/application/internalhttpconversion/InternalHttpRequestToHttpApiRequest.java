package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeaderPair;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpHeader;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpRequest;

public final class InternalHttpRequestToHttpApiRequest {

    private InternalHttpRequestToHttpApiRequest() {}

    public static HttpApiRequest convert(final InternalHttpRequest request) {
        return new HttpApiRequest(request.getPath())
                .setHeaders(request.getHeaders().asMap())
                .setBody(request.getBody())
                .setQueryParams(request.firstQueryParamValuesAsMap())
                .setRawQueryParams(request.firstQueryParamValuesAsMap())
                .setFilterableQueryParams(request.getRawQueryString())
                .setVerb(request.getMethod().name())
                .setUrl(request.getUrl())
                .setIP(request.getIP())
                .setUrlParams(request.getUrlParams())
                .setRawHeaders(rawHeadersList(request));
    }

    private static List<HttpHeaderPair> rawHeadersList(final InternalHttpRequest request) {
        List<HttpHeaderPair> headersList = new ArrayList<>();

        for (InternalHttpHeader header : request.getRawHeaders()) {
            headersList.add(new HttpHeaderPair(header.key(), header.value()));
        }

        return headersList;
    }
}
