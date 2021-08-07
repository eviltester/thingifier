package uk.co.compendiumdev.thingifier.api.http;

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

        if(accept.isMissing() || accept.isText()){
            // todo: have a config for enforce presence of content-type header, when false then derive content type from content when parsing message
            // todo: have a config for treatContentTypeTextAsMissingContentType - which is what this code current does
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
