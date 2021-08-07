package uk.co.compendiumdev.thingifier.api.http;

import java.util.*;

public final class HttpApiRequest {

    private String path="";
    private Map<String, String> headers;
    private String body="";
    private Map<String, String> queryParams; // only contains the first query param value
    private VERB verb;
    private String url="";
    private Map<String, String> rawQueryParams; // contains all the query param values e.g. ?p=1&p=2
    private String ip="";
    private Map<String, String>  urlParams;

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


    public enum VERB{ GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, CONNECT, TRACE}

    public HttpApiRequest(final String pathInfo) {
        this.path = justThePath(pathInfo);
        this.headers = new HashMap<>();
        queryParams = new HashMap<>();
        body = "";
        verb = VERB.GET;
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

    public Map<String, String> getHeaders() {
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
        String header = getHeader(headerName);
        if(header==null){
            header=aDefault;
        }
        return header;
    }

    public String getHeader(final String headerName) {
        return this.headers.get(headerName.toLowerCase());
    }



    public HttpApiRequest addHeader(final String headerName, final String headerValue) {
        this.headers.put(headerName.trim().toLowerCase(), headerValue.trim().toLowerCase());
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
