package uk.co.compendiumdev.thingifier.api.http;

import java.util.*;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeaderPair;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiMountSelection;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class HttpApiRequest {

    private String path = "";
    private String requestPath = "";
    private String mountedPath = "";
    private String mountName;
    private String mountPrefix = "";
    private boolean rewriteLocationHeadersToMount;
    private HttpHeadersBlock headers;
    private String body = "";
    private Map<String, String> queryParams; // only contains the first query param value
    private VERB verb;
    private String url = "";
    private Map<String, String> rawQueryParams; // contains all the query param values e.g. ?p=1&p=2

    private QueryFilterParams
            filterableQueryParams; // contains all the query param values in a form we can use for
    // sorting and filtering e.g. ?id>=1&id<=4
    private String ip = "";
    private Map<String, String> urlParams;

    // a storage for the raw headers, which might include duplicates
    private ArrayList<HttpHeaderPair> headersList;

    public void removePrefixFromPath(String prefix) {
        if (path.startsWith(prefix)) {
            path = justThePath(path.replaceFirst(prefix, ""));
        }
    }

    /**
     * Applies the public mount selected for this request.
     *
     * <p>The HTTP adapter uses {@link #getPath()} as the canonical Thingifier route path. The
     * original public path and mount metadata remain available to callbacks and hooks that need to
     * report what the client actually requested.
     *
     * @param mountSelection resolved mount selection
     */
    public void applyMountSelection(final ThingifierApiMountSelection mountSelection) {
        if (mountSelection == null) {
            return;
        }
        requestPath = justThePath(mountSelection.requestPath());
        mountedPath = justThePath(mountSelection.mountedPath());
        path = justThePath(mountSelection.internalPath());
        mountName = mountSelection.mountName();
        mountPrefix = mountSelection.mountPrefix();
        rewriteLocationHeadersToMount = mountSelection.shouldRewriteLocationHeaders();
    }

    public enum VERB {
        GET,
        HEAD,
        QUERY,
        POST,
        PUT,
        DELETE,
        PATCH,
        OPTIONS,
        CONNECT,
        TRACE
    }

    public HttpApiRequest(final String pathInfo) {
        path = justThePath(pathInfo);
        requestPath = path;
        mountedPath = path;
        headers = new HttpHeadersBlock();
        queryParams = new HashMap<>();
        filterableQueryParams = new QueryFilterParams();
        headersList = new ArrayList<>();
        body = "";
        verb = VERB.GET;
    }

    public HttpApiRequest setVerb(final String requestMethod) {
        verb = VERB.valueOf(requestMethod.toUpperCase());
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public Collection<String> getQueryParamNames() {
        return queryParams.keySet();
    }

    public String rawQueryParamsValue(final String queryParam) {
        return rawQueryParams.get(queryParam);
    }

    public HttpApiRequest setIP(final String ip) {
        this.ip = ip;
        return this;
    }

    public String getIP() {
        return this.ip;
    }

    public HttpApiRequest setUrlParams(final Map<String, String> params) {
        this.urlParams = new HashMap<>();
        this.urlParams.putAll(params);
        return this;
    }

    public String getUrlParam(final String paramKey) {
        return urlParams.get(paramKey);
    }

    public HttpApiRequest setFilterableQueryParams(String queryString) {
        filterableQueryParams = new UrlQueryParamParser().parse(queryString);
        return this;
    }

    public QueryFilterParams getFilterableQueryParams() {
        return filterableQueryParams;
    }

    public HttpApiRequest setRawHeaders(List<HttpHeaderPair> rawHeadersList) {
        headersList = new ArrayList<HttpHeaderPair>(rawHeadersList);
        return this;
    }

    public List<HttpHeaderPair> getHeadersList() {
        return new ArrayList<>(headersList);
    }

    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public HttpApiRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpApiRequest setHeaders(final Map<String, String> mapOfHeaderValues) {
        for (Map.Entry<String, String> header : mapOfHeaderValues.entrySet()) {
            addHeader(header.getKey(), header.getValue());
        }

        return this;
    }

    public String getPath() {
        return this.path;
    }

    /**
     * Returns the public request path as supplied to Thingifier before mount stripping.
     *
     * @return request path without a leading slash
     */
    public String getRequestPath() {
        return requestPath;
    }

    /**
     * Returns the public mounted path for this request.
     *
     * @return mounted path without a leading slash
     */
    public String getMountedPath() {
        return mountedPath;
    }

    /**
     * Returns the active mount name.
     *
     * @return mount name, or null when no named mount matched
     */
    public String getMountName() {
        return mountName;
    }

    /**
     * Returns the active public mount prefix.
     *
     * @return mount prefix with a leading slash, or empty when no prefix applies
     */
    public String getMountPrefix() {
        return mountPrefix;
    }

    /**
     * Reports whether this request matched a named mount.
     *
     * @return true when a named mount matched
     */
    public boolean hasActiveMount() {
        return mountName != null;
    }

    /**
     * Reports whether final relative Location headers should be rewritten to the active mount.
     *
     * @return true when the active mount requested Location rewriting
     */
    public boolean shouldRewriteLocationHeadersToMount() {
        return rewriteLocationHeadersToMount;
    }

    public HttpHeadersBlock getHeaders() {
        return this.headers;
    }

    public HttpApiRequest setBody(final String theBody) {
        this.body = theBody;
        return this;
    }

    public String getBody() {
        return this.body;
    }

    // common header requests Accept and Content-Type
    public String getAcceptHeader() {
        return getHeader("Accept", "");
    }

    public String getContentTypeHeader() {
        return getHeader("Content-Type", "");
    }

    public String getHeader(final String headerName, final String aDefault) {
        if (!this.headers.headerExists(headerName)) {
            return aDefault;
        }
        return getHeader(headerName);
    }

    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }

    public HttpApiRequest addHeader(final String headerName, final String headerValue) {
        final String trimmedHeaderValue = headerValue.trim();
        this.headers.put(headerName, trimmedHeaderValue);
        return this;
    }

    public HttpApiRequest setQueryParams(final Map<String, String> queryParamsAsMap) {
        queryParams = queryParamsAsMap;
        return this;
    }

    public HttpApiRequest setRawQueryParams(final Map<String, String> rawQueryParamsAsMap) {
        rawQueryParams = rawQueryParamsAsMap;
        return this;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public HttpApiRequest setVerb(final VERB verb) {
        this.verb = verb;
        return this;
    }

    public VERB getVerb() {
        return this.verb;
    }
}
