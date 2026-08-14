package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class ContentTypeHeaderValidator {
    private final ThingifierApiConfig apiConfig;
    private final Collection<String> xmlEntityNames;

    public ContentTypeHeaderValidator(final ThingifierApiConfig apiConfig) {
        this(apiConfig, List.of());
    }

    public ContentTypeHeaderValidator(
            final ThingifierApiConfig apiConfig, final Collection<String> xmlEntityNames) {
        this.apiConfig = apiConfig;
        this.xmlEntityNames = xmlEntityNames == null ? List.of() : List.copyOf(xmlEntityNames);
    }

    public ApiResponse validate(final String header) {
        // we don't validate content type header
        if (!this.apiConfig.willApiEnforceContentTypeHeaderForRequests()) {
            return null;
        }

        final ContentTypeHeaderParser accept = new ContentTypeHeaderParser(header);
        final boolean isXml = accept.isXML(xmlEntityNames);

        if (accept.isMissing()
                || (!isXml
                        && accept.isText()
                        && apiConfig.willAllowJsonAsDefaultContentType())) {
            // assume that we can derive content type from the actual content
            return null;
        }

        int statusContentTypeNotSupported = this.apiConfig.statusCodes().contentTypeNotSupported();

        if (!isXml && !accept.isJSON()) {
            return ApiResponse.error(
                    statusContentTypeNotSupported, "Unsupported Content Type - " + header);
        }

        if (isXml && !this.apiConfig.willAcceptXMLContent()) {
            return ApiResponse.error(statusContentTypeNotSupported, "XML Not Supported");
        }

        if (accept.isJSON() && !this.apiConfig.willAcceptJSONContent()) {
            return ApiResponse.error(statusContentTypeNotSupported, "JSON Not Supported");
        }

        return null;
    }
}
