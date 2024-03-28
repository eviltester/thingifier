package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;

/**
 * Given an internal ApiResponse create an HTTP abstraction response
 * The requestHeaders are used to configure the content type using the Accept header
 */
final public class HttpApiResponse {

    private final ApiResponse apiResponse;
    private final HttpHeadersBlock apiResponseHeaders;
    private final JsonThing jsonThing;
    private final ThingifierApiConfig apiConfig;

    private String type;
    private boolean asJson;

    public HttpApiResponse(final HttpHeadersBlock requestHeaders,
                           final ApiResponse anApiResponse,
                           JsonThing jsonThing,
                           ThingifierApiConfig apiConfig
                           ) {
        this.apiResponse = anApiResponse;
        this.apiResponseHeaders = new HttpHeadersBlock();
        this.jsonThing = jsonThing;
        this.apiConfig = apiConfig;
        asJson=true;

        HttpHeadersBlock useRequestHeaders = requestHeaders==null ? new HttpHeadersBlock() : requestHeaders;
        HttpHeadersBlock useApiResponseHeaders = anApiResponse==null ? new HttpHeadersBlock() : anApiResponse.getHeaders();

        configureFrom(useRequestHeaders, useApiResponseHeaders);
    }

    private void configureFrom(final HttpHeadersBlock requestHeaders, final HttpHeadersBlock originalApiResponseHeaders) {

        String acceptHeader = requestHeaders.get("accept");

        AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        if(accept.hasAPreferenceForXml()){
            if(apiConfig.willApiAllowXmlForResponses()) {
                asJson = false;
            }
        }

        if(!apiConfig.willApiAllowJsonForResponses()){
            asJson=false;
        }

        // TODO: handle text/plain, text/html
        if (asJson) {
            type = "application/json";
        } else {
            type = "application/xml";
        }

        apiResponseHeaders.putAll(originalApiResponseHeaders);
        apiResponseHeaders.put("Content-Type", type);


        if(apiConfig.willPreventRobotsFromIndexingResponse()) {
            apiResponseHeaders.put("x-robots-tag", "noindex");
        }

    }

    // TODO: handle text/plain, text/html
    public String getBody() {

        String returnBody = "";

        if(apiResponse.hasABodyOverride()){
            return apiResponse.getBody();
        }
        if (asJson) {
            returnBody = new ApiResponseAsJson(apiResponse, jsonThing).getJson();
        } else {
            returnBody = new ApiResponseAsXml(apiResponse, jsonThing).getXml();
        }

        return returnBody;
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

    public HttpHeadersBlock getHeaders() {
        return apiResponseHeaders;
    }

    public ApiResponse apiResponse() {
        return apiResponse;
    }
}
