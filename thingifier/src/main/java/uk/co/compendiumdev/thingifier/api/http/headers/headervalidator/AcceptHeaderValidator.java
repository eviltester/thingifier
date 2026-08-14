package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class AcceptHeaderValidator {
    private final ThingifierApiConfig apiConfig;
    private final Collection<String> xmlEntityNames;

    public AcceptHeaderValidator(final ThingifierApiConfig apiConfig) {
        this(apiConfig, List.of());
    }

    public AcceptHeaderValidator(
            final ThingifierApiConfig apiConfig, final Collection<String> xmlEntityNames) {
        this.apiConfig = apiConfig;
        this.xmlEntityNames = xmlEntityNames == null ? List.of() : List.copyOf(xmlEntityNames);
    }

    public ApiResponse validate(final String acceptHeader) {
        final AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);
        ApiResponse apiResponse = null;

        int statusAcceptTypeNotSupported = this.apiConfig.statusCodes().acceptTypeNotSupported();

        if (this.apiConfig.willApiEnforceAcceptHeaderForResponses()) {
            if (!accept.isSupportedHeader(xmlEntityNames)) {
                apiResponse =
                        ApiResponse.error(statusAcceptTypeNotSupported, "Unrecognised Accept Type");
            }
        }

        if (apiResponse == null && this.apiConfig.willApiEnforceAcceptHeaderForResponses()) {
            if (preferredAllowedResponseType(accept)
                    == AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE) {
                apiResponse =
                        ApiResponse.error(
                                statusAcceptTypeNotSupported,
                                unsupportedResponseTypeMessage(accept));
            }
        }

        return apiResponse;
    }

    private AcceptHeaderParser.ACCEPT_TYPE preferredAllowedResponseType(
            final AcceptHeaderParser accept) {
        return accept.preferredSupportedMediaType(
                        allowedResponseTypes(), defaultResponseType(), xmlEntityNames)
                .type();
    }

    private List<AcceptHeaderParser.ACCEPT_TYPE> allowedResponseTypes() {
        final List<AcceptHeaderParser.ACCEPT_TYPE> allowedTypes = new ArrayList<>();
        for (AcceptHeaderParser.ACCEPT_TYPE type :
                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes()) {
            if (type.rendersAsXml() && !this.apiConfig.willApiAllowXmlForResponses()) {
                continue;
            }
            if (type == AcceptHeaderParser.ACCEPT_TYPE.JSON
                    && !this.apiConfig.willApiAllowJsonForResponses()) {
                continue;
            }
            allowedTypes.add(type);
        }
        return allowedTypes;
    }

    private AcceptHeaderParser.ACCEPT_TYPE defaultResponseType() {
        if (apiConfig.willApiAllowJsonForResponses()) {
            return AcceptHeaderParser.ACCEPT_TYPE.JSON;
        }
        if (apiConfig.willApiAllowXmlForResponses()) {
            return AcceptHeaderParser.ACCEPT_TYPE.XML;
        }
        return AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE;
    }

    private String unsupportedResponseTypeMessage(final AcceptHeaderParser accept) {
        if (accept.hasAskedForXmlResponse(xmlEntityNames) && !accept.willAcceptJson()) {
            return "XML not supported";
        }
        if (accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON) && !accept.willAcceptXml()) {
            return "JSON not supported";
        }
        return "No acceptable response type supported";
    }
}
