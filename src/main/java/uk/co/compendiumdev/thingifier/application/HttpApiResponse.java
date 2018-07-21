package uk.co.compendiumdev.thingifier.application;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;

import java.util.Map;
import java.util.Set;

public class HttpApiResponse {

    private final ApiResponse apiResponse;
    private final Set<String> headers;
    private String type;

    public HttpApiResponse(final Set<String> requestHeaders, final ApiResponse anApiResponse) {
        this.headers = requestHeaders;
        this.apiResponse = anApiResponse;
        type = null;
    }

    public String getBody() {

        boolean asJson = true; //default to json

        String acceptHeader = getHeader("Accept");

        if (acceptHeader != null) {
            if (acceptHeader.endsWith("/xml")) {
                asJson = false;
            }
//            if (request.headers("Accept").endsWith("/json")) {
//                // todo : should probably return a 406 Not Acceptable http status message if we don't support the asked for type
//            }
        }

        String returnBody = "";

        if (asJson) {
            returnBody = new ApiResponseAsJson(apiResponse).getJson();

            type = "application/json";
        } else {
            returnBody = new ApiResponseAsXml(apiResponse).getXml();
            type = "application/xml";
        }

        return returnBody;
    }

    private String getHeader(final String name) {


        for (String header : headers) {

            String[] headerParts = header.split(":");

            if (headerParts.length == 2) {
                if (headerParts[0].trim().equalsIgnoreCase(name)) {
                    return headerParts[1].trim();
                }
            }
        }

        return null;
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

    public Set<Map.Entry<String, String>> getHeaders() {
        return apiResponse.getHeaders();
    }
}
