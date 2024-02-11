package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

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
        ApiResponse apiResponse=null;

        int statusAcceptTypeNotSupported = this.apiConfig.statusCodes().acceptTypeNotSupported();

        if(this.apiConfig.willApiEnforceAcceptHeaderForResponses()){
            if (!accept.isSupportedHeader()){
                apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "Unrecognised Accept Type");
            }
        }

        boolean willOnlyAcceptXML = accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML) &&
                !accept.willAcceptJson();
        if (    willOnlyAcceptXML &&
                !this.apiConfig.willApiAllowXmlForResponses() &&
                this.apiConfig.willApiEnforceAcceptHeaderForResponses()
        ) {
            apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "XML not supported");
        }

        boolean willOnlyAcceptJSON = accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON) &&
                !accept.willAcceptXml();
        if (    willOnlyAcceptJSON &&
                !this.apiConfig.willApiAllowJsonForResponses() &&
                this.apiConfig.willApiEnforceAcceptHeaderForResponses()
        ) {
            apiResponse = ApiResponse.error(statusAcceptTypeNotSupported, "JSON not supported");
        }

        return apiResponse;
    }
}
