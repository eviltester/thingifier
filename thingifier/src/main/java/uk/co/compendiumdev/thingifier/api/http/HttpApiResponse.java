package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsDelimitedText;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsHtml;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJsonLines;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsPlainText;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

/**
 * Given an internal ApiResponse create an HTTP abstraction response The requestHeaders are used to
 * configure the content type using the Accept header
 */
public final class HttpApiResponse {

    private final ApiResponse apiResponse;
    private final HttpHeadersBlock apiResponseHeaders;
    private final JsonThing jsonThing;
    private final ThingifierApiConfig apiConfig;

    private String type;
    private AcceptHeaderParser.ACCEPT_TYPE responseType;

    public HttpApiResponse(
            final HttpHeadersBlock requestHeaders,
            final ApiResponse anApiResponse,
            JsonThing jsonThing,
            ThingifierApiConfig apiConfig) {
        this.apiResponse = anApiResponse;
        this.apiResponseHeaders = new HttpHeadersBlock();
        this.jsonThing = jsonThing;
        this.apiConfig = apiConfig;
        responseType = AcceptHeaderParser.ACCEPT_TYPE.JSON;

        HttpHeadersBlock useRequestHeaders =
                requestHeaders == null ? new HttpHeadersBlock() : requestHeaders;
        HttpHeadersBlock useApiResponseHeaders =
                anApiResponse == null ? new HttpHeadersBlock() : anApiResponse.getHeaders();

        configureFrom(useRequestHeaders, useApiResponseHeaders);
    }

    private void configureFrom(
            final HttpHeadersBlock requestHeaders,
            final HttpHeadersBlock originalApiResponseHeaders) {

        String acceptHeader = requestHeaders.get("accept");

        AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        responseType = selectResponseType(accept);
        type = responseType.mediaType();

        apiResponseHeaders.putAll(originalApiResponseHeaders);
        apiResponseHeaders.put("Content-Type", type);

        if (apiConfig.willPreventRobotsFromIndexingResponse()) {
            apiResponseHeaders.put("x-robots-tag", "noindex");
        }
    }

    private AcceptHeaderParser.ACCEPT_TYPE selectResponseType(final AcceptHeaderParser accept) {
        for (AcceptHeaderParser.ACCEPT_TYPE candidate :
                accept.getSupportedTypesInPreferenceOrder()) {
            if (candidate == AcceptHeaderParser.ACCEPT_TYPE.ANYTHING) {
                continue;
            }
            if (canRender(candidate)) {
                return candidate;
            }
        }
        return defaultResponseType();
    }

    private boolean canRender(final AcceptHeaderParser.ACCEPT_TYPE candidate) {
        if (candidate == AcceptHeaderParser.ACCEPT_TYPE.XML) {
            return apiConfig.willApiAllowXmlForResponses();
        }
        if (candidate == AcceptHeaderParser.ACCEPT_TYPE.JSON) {
            return apiConfig.willApiAllowJsonForResponses();
        }
        return candidate != AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE;
    }

    private AcceptHeaderParser.ACCEPT_TYPE defaultResponseType() {
        if (apiConfig.willApiAllowJsonForResponses()) {
            return AcceptHeaderParser.ACCEPT_TYPE.JSON;
        }
        return AcceptHeaderParser.ACCEPT_TYPE.XML;
    }

    public String getBody() {
        if (apiResponse.hasABodyOverride()) {
            return apiResponse.getBody();
        }

        switch (responseType) {
            case XML:
                return new ApiResponseAsXml(apiResponse, jsonThing).getXml();
            case CSV:
                return new ApiResponseAsDelimitedText(apiResponse, ',').getText();
            case TEXT:
                return new ApiResponseAsPlainText(apiResponse).getText();
            case HTML:
                return new ApiResponseAsHtml(apiResponse).getHtml();
            case NDJSON:
            case JSONL:
                return new ApiResponseAsJsonLines(apiResponse, jsonThing).getJsonLines();
            case JSON_SEQ:
                return new ApiResponseAsJsonLines(apiResponse, jsonThing).getJsonSequence();
            case TSV:
                return new ApiResponseAsDelimitedText(apiResponse, '\t').getText();
            case JSON:
            default:
                return new ApiResponseAsJson(apiResponse, jsonThing).getJson();
        }
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
