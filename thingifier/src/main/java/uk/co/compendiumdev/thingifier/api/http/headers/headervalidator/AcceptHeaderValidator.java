package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.ArrayList;
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
        return accept.preferredSupportedType(allowedResponseTypes(), defaultResponseType());
    }

    private List<AcceptHeaderParser.ACCEPT_TYPE> allowedResponseTypes() {
        final List<AcceptHeaderParser.ACCEPT_TYPE> allowedTypes = new ArrayList<>();
        for (AcceptHeaderParser.ACCEPT_TYPE type :
                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes()) {
            if (type == AcceptHeaderParser.ACCEPT_TYPE.XML
                    && !this.apiConfig.willApiAllowXmlForResponses()) {
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
        if (accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML) && !accept.willAcceptJson()) {
            return "XML not supported";
        }
        if (accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON) && !accept.willAcceptXml()) {
            return "JSON not supported";
        }
        return "No acceptable response type supported";
    }
}
