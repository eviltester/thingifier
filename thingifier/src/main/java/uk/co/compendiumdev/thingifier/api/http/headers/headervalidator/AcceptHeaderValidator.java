package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class AcceptHeaderValidator {
    private final ThingifierApiConfig apiConfig;

    public AcceptHeaderValidator(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ApiResponse validate(final String acceptHeader) {
        final AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);
        ApiResponse apiResponse = null;

        int statusAcceptTypeNotSupported = this.apiConfig.statusCodes().acceptTypeNotSupported();

        if (this.apiConfig.willApiEnforceAcceptHeaderForResponses()) {
            if (!accept.isSupportedHeader()) {
                apiResponse =
                        ApiResponse.error(statusAcceptTypeNotSupported, "Unrecognised Accept Type");
            }
        }

        if (apiResponse == null && this.apiConfig.willApiEnforceAcceptHeaderForResponses()) {
            if (!hasAnyAllowedResponseType(accept)) {
                apiResponse =
                        ApiResponse.error(
                                statusAcceptTypeNotSupported,
                                unsupportedResponseTypeMessage(accept));
            }
        }

        return apiResponse;
    }

    private boolean hasAnyAllowedResponseType(final AcceptHeaderParser accept) {
        final List<AcceptHeaderParser.ACCEPT_TYPE> supportedTypes =
                accept.getSupportedTypesInPreferenceOrder();
        if (supportedTypes.isEmpty()) {
            return true;
        }

        for (AcceptHeaderParser.ACCEPT_TYPE supportedType : supportedTypes) {
            if (supportedType == AcceptHeaderParser.ACCEPT_TYPE.ANYTHING) {
                return true;
            }
            if (supportedType == AcceptHeaderParser.ACCEPT_TYPE.XML
                    && !this.apiConfig.willApiAllowXmlForResponses()) {
                continue;
            }
            if (supportedType == AcceptHeaderParser.ACCEPT_TYPE.JSON
                    && !this.apiConfig.willApiAllowJsonForResponses()) {
                continue;
            }
            return true;
        }

        return false;
    }

    private String unsupportedResponseTypeMessage(final AcceptHeaderParser accept) {
        if (accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML) && !accept.willAcceptJson()) {
            return "XML not supported";
        }
        if (accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON) && !accept.willAcceptXml()) {
            return "JSON not supported";
        }
        return "No acceptable response type supported";
    }
}
