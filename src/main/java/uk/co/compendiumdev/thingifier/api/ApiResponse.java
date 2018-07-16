package uk.co.compendiumdev.thingifier.api;

import com.google.gson.Gson;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiResponse {
    private final int statusCode;
    private String body;
    private Map<String, String> headers;

    public ApiResponse(int statusCode) {
        this.statusCode = statusCode;
        headers = new HashMap<>();
    }

    public static ApiResponse created(ThingInstance thingInstance) {
        return new ApiResponse(201).
                                setBody(JsonThing.asJson(thingInstance)).
                                setLocationHeader(thingInstance.getEntity().getName() + "/" + thingInstance.getGUID());
    }

    private ApiResponse setLocationHeader(String location) {
        this.headers.put("Location", location);
        return this;
    }

    public boolean hasHeaders(){
        return headers.size()>0;
    }

    public Set<Map.Entry<String, String>> getHeaders(){
        return headers.entrySet();
    }

    public static ApiResponse error404(String errorMessage) {
        return new ApiResponse(404).setBody(getErrorMessageJson(errorMessage));
    }

    public static String getErrorMessageJson(String errorMessage) {
        Map error = new HashMap<String,String>();
        error.put("errorMessage", errorMessage);
        return new Gson().toJson(error);

    }

    public static String getErrorMessageJson(Collection<String> errorMessages) {
        Map error = new HashMap<String,String>();
        error.put("errorMessages", errorMessages);
        return new Gson().toJson(error);

    }

    public static ApiResponse success(String body) {
        return new ApiResponse(200).setBody(body);
    }

    public static ApiResponse error(int statusCode, String errorMessage) {
        return new ApiResponse(statusCode).setBody(getErrorMessageJson(errorMessage));
    }

    public static ApiResponse error(int statusCode, Collection<String> errorMessages) {
        return new ApiResponse(statusCode).setBody(getErrorMessageJson(errorMessages));
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
