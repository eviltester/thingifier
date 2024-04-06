package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.StringPair;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.fromurl.UrlParamParser;

import java.util.*;

public final class HttpApiRequest {

    private String path="";
    private HttpHeadersBlock headers;
    private String body="";
    private Map<String, String> queryParams; // only contains the first query param value
    private VERB verb;
    private String url="";
    private Map<String, String> rawQueryParams; // contains all the query param values e.g. ?p=1&p=2

    private QueryFilterParams filterableQueryParams; // contains all the query param values in a form we can use for sorting and filtering e.g. ?id>=1&id<=4
    private String ip="";
    private Map<String, String>  urlParams;

    // a storage for the raw headers, which might include duplicates
    private ArrayList<StringPair> headersList;

    public void removePrefixFromPath(String prefix) {
        if(path.startsWith(prefix)){
            path = justThePath(path.replaceFirst(prefix,""));
        }
    }

    public enum VERB{ GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, CONNECT, TRACE}

    public HttpApiRequest(final String pathInfo) {
        path = justThePath(pathInfo);
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
        this.ip=ip;
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
        filterableQueryParams = new UrlParamParser().parse(queryString);
        return this;
    }

    public QueryFilterParams getFilterableQueryParams() {
        return filterableQueryParams;
    }

    public HttpApiRequest setRawHeaders(List<StringPair> rawHeadersList) {
        headersList = new ArrayList<StringPair>(rawHeadersList);
        return this;
    }

    public List<StringPair> getHeadersList() {
        return new ArrayList<>(headersList);
    }



    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public HttpApiRequest setUrl(String url){
        this.url = url;
        return this;
    }

    public HttpApiRequest setHeaders(final Map<String, String> mapOfHeaderValues) {
        for(Map.Entry<String, String>header : mapOfHeaderValues.entrySet()){
            addHeader(header.getKey(), header.getValue());
        }

        return this;
    }

    public String getPath() {
        return this.path;
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
        if(!this.headers.headerExists(headerName)){
            return aDefault;
        }
        return getHeader(headerName);
    }

    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }

    public HttpApiRequest addHeader(final String headerName, final String headerValue) {
        this.headers.put(headerName, headerValue.trim().toLowerCase());
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
