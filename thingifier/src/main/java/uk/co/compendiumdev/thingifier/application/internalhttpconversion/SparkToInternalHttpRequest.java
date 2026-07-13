package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import spark.Request;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpRequest;

public final class SparkToInternalHttpRequest {

    private SparkToInternalHttpRequest() {}

    public static InternalHttpRequest convert(final Request request) {
        InternalHttpRequest internalRequest =
                new InternalHttpRequest(request.pathInfo())
                        .setBody(request.body())
                        .setQueryParams(queryParamsAsMap(request))
                        .setRawQueryString(request.queryString())
                        .setMethod(request.requestMethod())
                        .setUrl(request.url())
                        .setIP(request.ip())
                        .setUrlParams(request.params());

        addRawHeaders(internalRequest, request.raw());

        return internalRequest;
    }

    private static void addRawHeaders(
            final InternalHttpRequest internalRequest, final HttpServletRequest raw) {

        for (Enumeration<String> headerNames = raw.getHeaderNames();
                headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement();
            for (Enumeration<String> headerValues = raw.getHeaders(headerName);
                    headerValues.hasMoreElements(); ) {
                internalRequest.addHeader(headerName, headerValues.nextElement());
            }
        }
    }

    private static Map<String, List<String>> queryParamsAsMap(final Request request) {

        Map<String, List<String>> params = new LinkedHashMap<>();

        for (String paramName : request.queryParams()) {
            List<String> paramValues = new ArrayList<>();
            String[] values = request.queryParamsValues(paramName);
            if (values == null || values.length == 0) {
                paramValues.add("");
            } else {
                for (String value : values) {
                    paramValues.add(value == null ? "" : value);
                }
            }
            params.put(paramName, paramValues);
        }

        return params;
    }
}
