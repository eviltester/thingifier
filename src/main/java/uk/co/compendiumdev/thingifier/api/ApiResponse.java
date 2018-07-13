package uk.co.compendiumdev.thingifier.api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private final int statusCode;
    private String body;

    public ApiResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public static ApiResponse created(String body) {
        return new ApiResponse(201).setBody(body);
    }

    public static ApiResponse error404(String errorMessage) {
        return new ApiResponse(404).setBody(getErrorMessageJson(errorMessage));
    }

    public static String getErrorMessageJson(String errorMessage) {
        Map error = new HashMap<String,String>();
        error.put("errorMessage", errorMessage);
        return new Gson().toJson(error);

    }

    public static ApiResponse success(String body) {
        return new ApiResponse(200).setBody(body);
    }

    public static ApiResponse error(int statusCode, String errorMessage) {
        return new ApiResponse(statusCode).setBody(getErrorMessageJson(errorMessage));
    }

    private ApiResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getBody() {
        return body;
    }
}
