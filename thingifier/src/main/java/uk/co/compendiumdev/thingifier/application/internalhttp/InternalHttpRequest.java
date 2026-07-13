package uk.co.compendiumdev.thingifier.application.internalhttp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InternalHttpRequest {

    private String path;
    private String body;
    private String url;
    private String ip;
    private String rawQueryString;
    private InternalHttpMethod method;
    private final InternalHttpHeaders headers;
    private final List<InternalHttpHeader> rawHeaders;
    private Map<String, List<String>> queryParams;
    private Map<String, String> urlParams;

    public InternalHttpRequest(final String path) {
        this.path = justThePath(path == null ? "" : path);
        this.body = "";
        this.url = "";
        this.ip = "";
        this.rawQueryString = "";
        this.method = InternalHttpMethod.GET;
        this.headers = new InternalHttpHeaders();
        this.rawHeaders = new ArrayList<>();
        this.queryParams = new LinkedHashMap<>();
        this.urlParams = new LinkedHashMap<>();
    }

    public InternalHttpRequest setPath(final String path) {
        this.path = justThePath(path == null ? "" : path);
        return this;
    }

    public String getPath() {
        return path;
    }

    public InternalHttpRequest setBody(final String body) {
        this.body = body == null ? "" : body;
        return this;
    }

    public String getBody() {
        return body;
    }

    public InternalHttpRequest setUrl(final String url) {
        this.url = url == null ? "" : url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public InternalHttpRequest setIP(final String ip) {
        this.ip = ip == null ? "" : ip;
        return this;
    }

    public String getIP() {
        return ip;
    }

    public InternalHttpRequest setRawQueryString(final String rawQueryString) {
        this.rawQueryString = rawQueryString == null ? "" : rawQueryString;
        return this;
    }

    public String getRawQueryString() {
        return rawQueryString;
    }

    public InternalHttpRequest setMethod(final String method) {
        this.method = InternalHttpMethod.from(method);
        return this;
    }

    public InternalHttpRequest setVerb(final String method) {
        return setMethod(method);
    }

    public InternalHttpRequest setVerb(final InternalHttpMethod method) {
        this.method = method == null ? InternalHttpMethod.GET : method;
        return this;
    }

    public InternalHttpMethod getMethod() {
        return method;
    }

    public InternalHttpMethod getVerb() {
        return method;
    }

    public InternalHttpRequest addHeader(final String headerName, final String headerValue) {
        headers.put(headerName, headerValue);
        rawHeaders.add(new InternalHttpHeader(headerName, headerValue));
        return this;
    }

    public InternalHttpRequest setHeaders(final Map<String, String> headersToSet) {
        if (headersToSet == null) {
            return this;
        }

        for (Map.Entry<String, String> header : headersToSet.entrySet()) {
            addHeader(header.getKey(), header.getValue());
        }
        return this;
    }

    public InternalHttpHeaders getHeaders() {
        InternalHttpHeaders copy = new InternalHttpHeaders();
        copy.putAll(headers);
        return copy;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }

    public String getHeader(final String headerName, final String defaultValue) {
        if (!headers.headerExists(headerName)) {
            return defaultValue;
        }
        return getHeader(headerName);
    }

    public boolean hasHeader(final String headerName) {
        return headers.headerExists(headerName);
    }

    public String getAcceptHeader() {
        return getHeader("Accept", "");
    }

    public String getContentTypeHeader() {
        return getHeader("Content-Type", "");
    }

    public List<InternalHttpHeader> getRawHeaders() {
        return new ArrayList<>(rawHeaders);
    }

    public InternalHttpRequest setQueryParams(final Map<String, List<String>> params) {
        this.queryParams = new LinkedHashMap<>();
        if (params == null) {
            return this;
        }

        for (Map.Entry<String, List<String>> param : params.entrySet()) {
            addQueryParamValues(param.getKey(), param.getValue());
        }
        return this;
    }

    public InternalHttpRequest addQueryParam(final String paramName, final String paramValue) {
        queryParams
                .computeIfAbsent(paramName, key -> new ArrayList<>())
                .add(nullToEmpty(paramValue));
        return this;
    }

    public Map<String, List<String>> getQueryParams() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> param : queryParams.entrySet()) {
            copy.put(param.getKey(), new ArrayList<>(param.getValue()));
        }
        return copy;
    }

    public Map<String, String> firstQueryParamValuesAsMap() {
        Map<String, String> firstValues = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> param : queryParams.entrySet()) {
            if (param.getValue().isEmpty()) {
                firstValues.put(param.getKey(), "");
            } else {
                firstValues.put(param.getKey(), param.getValue().get(0));
            }
        }
        return firstValues;
    }

    public InternalHttpRequest setUrlParams(final Map<String, String> params) {
        this.urlParams = new LinkedHashMap<>();
        if (params != null) {
            this.urlParams.putAll(params);
        }
        return this;
    }

    public Map<String, String> getUrlParams() {
        return new LinkedHashMap<>(urlParams);
    }

    public String getUrlParam(final String paramKey) {
        return urlParams.get(paramKey);
    }

    private void addQueryParamValues(final String paramName, final List<String> values) {
        if (values == null || values.isEmpty()) {
            addQueryParam(paramName, "");
            return;
        }

        for (String value : values) {
            addQueryParam(paramName, value);
        }
    }

    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
