package uk.co.compendiumdev.challenger.http.httpclient;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseDetails {

    public int statusCode;
    public String body;
    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        for(Map.Entry<String, String>given : headers.entrySet()){
            this.headers.put(given.getKey().toUpperCase(), given.getValue());
        }
    }

    public String getHeader(final String headername) {
        return headers.get(headername.toUpperCase());
    }
}
