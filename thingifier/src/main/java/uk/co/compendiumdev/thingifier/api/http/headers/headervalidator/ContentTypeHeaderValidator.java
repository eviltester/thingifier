package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class ContentTypeHeaderValidator {
    private final ThingifierApiConfig apiConfig;

    public ContentTypeHeaderValidator(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ApiResponse validate(final String header) {
        // we don't validate content type header
        if(!this.apiConfig.willApiEnforceContentTypeHeaderForRequests()){
            return null;
        }

        final ContentTypeHeaderParser accept = new ContentTypeHeaderParser(header);

        if(accept.isMissing() || accept.isText() && apiConfig.willAllowJsonAsDefaultContentType()){
            // assume that we can derive content type from the actual content
            return null;
        }

        int statusContentTypeNotSupported = this.apiConfig.statusCodes().
                contentTypeNotSupported();

        if(!accept.isXML() && !accept.isJSON()){
            return ApiResponse.error(statusContentTypeNotSupported,
                    "Unsupported Content Type - " + header);
        }

        if(accept.isXML() && !this.apiConfig.willAcceptXMLContent()){
            return ApiResponse.error(statusContentTypeNotSupported, "XML Not Supported");
        }

        if(accept.isJSON() && !this.apiConfig.willAcceptJSONContent()){
            return ApiResponse.error(statusContentTypeNotSupported, "JSON Not Supported");
        }

        return null;
    }
}
