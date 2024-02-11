package uk.co.compendiumdev.thingifier.api.http.headers;

import java.util.HashMap;
import java.util.Map;

public class HttpHeadersBlock {

    final Map<String,String> headers;

    public HttpHeadersBlock(){
        headers = new HashMap<>();
    }
    public void put(String headername, String value) {
        if(headername==null){return;}

        String valueToAdd = value==null ? "" : value;

        // header names are case insensitive
        headers.put(headername.trim().toLowerCase(), valueToAdd);
    }

    public String get(String headername) {

        if(headername==null){
            return "";
        }

        if (!headers.containsKey(headername.toLowerCase())) {
            return "";
        }

        return headers.get(headername.toLowerCase());
    }

    public Map<String, String> asMap() {
        return new HashMap<>(headers);
    }

    public void putAll(Map<String, String> headers) {

        if(headers==null){return;}

        for( Map.Entry<String, String> entry : headers.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void putAll(HttpHeadersBlock originalApiResponseHeaders) {
        putAll(originalApiResponseHeaders.asMap());
    }

    public int size() {
        return headers.size();
    }

    public boolean headerExists(String headerName) {
        if(headerName==null){
            return false;
        }
        return headers.containsKey(headerName.trim().toLowerCase());
    }
}
