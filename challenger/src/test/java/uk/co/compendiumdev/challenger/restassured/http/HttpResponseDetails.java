package uk.co.compendiumdev.challenger.restassured.http;

import com.google.gson.Gson;

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

        this.headers.putAll(headers);
    }

}
