package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.HashMap;
import java.util.Map;

final public class HttpApiResponse {

    private final ApiResponse apiResponse;
    private final HashMap<String, String> apiResponseHeaders;
    private final JsonThing jsonThing;
    private final ThingifierApiConfig apiConfig;

    private String type;
    private boolean asJson;

    public HttpApiResponse(final Map<String, String> requestHeaders,
                           final ApiResponse anApiResponse,
                           JsonThing jsonThing,
                           ThingifierApiConfig apiConfig
                           ) {
        this.apiResponse = anApiResponse;
        this.apiResponseHeaders = new HashMap<String, String>();
        this.jsonThing = jsonThing;
        this.apiConfig = apiConfig;
        asJson=true;

        if(requestHeaders==null){
            configure(new HashMap<>());
        }else {
            configure(requestHeaders);
        }

    }

    private void configure(final Map<String, String> requestHeaders) {

        String acceptHeader = getHeader("Accept", requestHeaders);

        AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        if(accept.hasAPreferenceForXml()){
            if(apiConfig.willApiAllowXmlForResponses()) {
                asJson = false;
            }
        }

        if(!apiConfig.willApiAllowJsonForResponses()){
            asJson=false;
        }

        if (asJson) {
            type = "application/json";
        } else {
            type = "application/xml";
        }
        apiResponseHeaders.put("Content-Type", type);
        if(apiResponse!=null) {
            apiResponseHeaders.putAll(apiResponse.getHeaders());
        }
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

        if(requestHeaders==null){
            return "";
        }

        if (requestHeaders.containsKey(name.toLowerCase())) {
            return requestHeaders.get(name.toLowerCase());
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

    public ApiResponse apiResponse() {
        return apiResponse;
    }
}
