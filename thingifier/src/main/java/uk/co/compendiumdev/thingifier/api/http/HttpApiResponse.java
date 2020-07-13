package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.HashMap;
import java.util.Map;

final public class HttpApiResponse {

    private final ApiResponse apiResponse;
    private final HashMap<String, String> apiResponseHeaders;
    private final JsonThing jsonThing;

    private String type;
    private boolean asJson;

    public HttpApiResponse(final Map<String, String> requestHeaders,
                           final ApiResponse anApiResponse,
                           JsonThing jsonThing
                           ) {
        this.apiResponse = anApiResponse;
        this.apiResponseHeaders = new HashMap<String, String>();
        this.jsonThing = jsonThing;
        configure(requestHeaders);
    }

    private void configure(final Map<String, String> requestHeaders) {
        asJson = true; //default to json

        String acceptHeader = getHeader("Accept", requestHeaders);

        if (acceptHeader.endsWith("/xml")) {
            asJson = false;
        }


        if (asJson) {
            type = "application/json";
        } else {
            type = "application/xml";
        }
        apiResponseHeaders.put("Content-Type", type);
        apiResponseHeaders.putAll(apiResponse.getHeaders());
    }

    public String getBody() {

        String returnBody = "";

        if (asJson) {
            returnBody = new ApiResponseAsJson(apiResponse, jsonThing).getJson();
        } else {
            returnBody = new ApiResponseAsXml(apiResponse, jsonThing).getXml();
        }

        return returnBody;
    }

    private String getHeader(final String name, Map<String, String> requestHeaders) {

        if (requestHeaders.containsKey(name)) {
            return requestHeaders.get(name);
        }
        return "";
    }

    public boolean hasType() {
        return this.type != null;
    }

    public String getType() {
        return this.type;
    }

    public int getStatusCode() {
        return apiResponse.getStatusCode();
    }

    public Map<String, String> getHeaders() {
        return apiResponse.getHeaders();
    }
}
