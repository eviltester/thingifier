package uk.co.compendiumdev.thingifier.apiconfig;

public class ThingifierApiConfig {


    // Params
    // todo: willAllowFilteringThroughUrlParams  true/false (default: true)
    // todo: willEnforceFilteringThroughUrlParams true/false ie. 404 error if params when not supported (default: true)
    // todo: api request level allow filtering e.g. on some /things allow filtering but not others
    private ParamConfig paramsConfig;

    // Plurals
    // todo: enforcePluralsInApiCalls true/false i.e. throw404ErrorIfWrongPluralSingluarUsed default is true
    // - showUrlsAsPluralOrSingular, allow toggling between plural or singular
    private boolean willShowSingleInstancesAsPlural;

    // XML
    // willApiAllowXMLResponses i.e. respond with XML if asked for, json otherwise (default: true)
    boolean willApiAllowXmlResponses;
    // todo: willApiAllowXMLRequests i.e. process when XML content submitted, if not then throw error (default: true)

    // JSON
    // todo: willApiAllowJsonResponses i.e. respond with Json if asked for, xml otherwise
    boolean willApiAllowJsonResponses;

    // todo: willApiAllowJsonRequests i.e. process when Json content submitted, if not then throw error

    // willApiEnforceAcceptHeaderForResponses i.e. respond with 415 if asked for but not allowed XML responses (default: true)
    boolean willApiEnforceAcceptHeaderForResponses;

    /*
        *** Responses ***
     */
    private JsonOutputConfig jsonOutputConfig;
    // willShowFieldvaluesAsConverted Types e.g. boolean is `true` instead of "true"

    // willShowIdsIfAvailable e.g. instead of GUIDs everywhere, only use GUID if an ID not available
    private boolean willShowIdsInResponsesIfAvailable;

    // willShowGuidsInResponses e.g. if false then no GUIDs shown in responses at all
    private boolean willShowGuidsInResponses;




    // Requests
    // willEnforceDeclaredTypesInInput e.g. only accept if boolean is given as `true` indead of "true"
    private boolean willEnforceDeclaredTypesInInput;

    // URLs
    // willUseIdsInUrlsIfAvailable - instead of GUIDs use Ids
    private boolean willShowIdsInUrlsIfAvailable;
    private StatusCodeConfig statusCodeConfig;

    public ThingifierApiConfig(){


        // default to the most modern and 'up to date' config
        willShowSingleInstancesAsPlural=true;
        willShowGuidsInResponses=true;  // custom headers
        willShowIdsInResponsesIfAvailable = true; // relationship rendering
        willShowIdsInUrlsIfAvailable = true;  // location headers, api urls
        willEnforceDeclaredTypesInInput = true;

        willApiAllowXmlResponses = true;
        willApiAllowJsonResponses = true;
        willApiEnforceAcceptHeaderForResponses = false; // default to whatever is allowed by default


        paramsConfig = new ParamConfig();

        // by default
        jsonOutputConfig = new JsonOutputConfig();
        jsonOutputConfig.setShowGuidsInResponse(willShowGuidsInResponses);
        jsonOutputConfig.setCompressRelationships(true);
        jsonOutputConfig.setRelationshipsUseIdsIfAvailable(willShowIdsInResponsesIfAvailable);

        statusCodeConfig = new StatusCodeConfig();
    }

    public JsonOutputConfig jsonOutput() {
        return jsonOutputConfig;
    }


    public ThingifierApiConfig setResponsesToShowGuids(boolean allow){
        willShowGuidsInResponses = allow;
        if(!allow) {
            jsonOutput().setRelationshipsUseIdsIfAvailable(true);
            jsonOutput().setShowGuidsInResponse(false);
        }
        return this;
    }

    public ThingifierApiConfig setResponsesToShowIdsIfAvailable(boolean allow) {
        willShowIdsInResponsesIfAvailable = allow;
        jsonOutput().setRelationshipsUseIdsIfAvailable(allow);
        return this;
    }

    public ThingifierApiConfig setUrlToShowIdsInUrlsIfAvailable(boolean allow) {
        willShowIdsInUrlsIfAvailable = allow;
        jsonOutput().setRelationshipsUseIdsIfAvailable(allow);
        return this;
    }

    public ThingifierApiConfig setUrlToShowSingleInstancesAsPlural(boolean yes){
        willShowSingleInstancesAsPlural = yes;
        return this;
    }

    public ThingifierApiConfig setApiToEnforceDeclaredTypesInInput(boolean config){
        willEnforceDeclaredTypesInInput = config;
        return this;
    }
    public ThingifierApiConfig setApiToAllowXmlForResponses(boolean allow) {
        willApiAllowXmlResponses = allow;
        return this;
    }

    public ThingifierApiConfig setApiToAllowJsonForResponses(boolean allow) {
        willApiAllowJsonResponses = allow;
        return this;
    }

    public ThingifierApiConfig setApiToEnforceAcceptHeaderForResponses(boolean config) {
        willApiEnforceAcceptHeaderForResponses=config;
        return this;
    }

    public boolean willUrlShowInstancesAsPlural() {
        return willShowSingleInstancesAsPlural;
    }
    public boolean willResponseShowIdsIfAvailable() {
        return willShowIdsInResponsesIfAvailable;
    }
    public boolean willResponsesShowGuids() {
        return willShowGuidsInResponses;
    }
    public boolean willUrlsShowIdsIfAvailable() {
        return willShowIdsInUrlsIfAvailable;
    }
    public boolean willApiEnforceDeclaredTypesInInput() {
        return willEnforceDeclaredTypesInInput;
    }

    public boolean willApiAllowXmlForResponses() {
        return willApiAllowXmlResponses;
    }

    public boolean willApiAllowJsonForResponses() {
        return willApiAllowJsonResponses;
    }

    public boolean willApiEnforceAcceptHeaderForResponses() {
        return willApiEnforceAcceptHeaderForResponses;
    }

    public ParamConfig forParams() {
        return paramsConfig;
    }

    public StatusCodeConfig statusCodes() {
        return statusCodeConfig;
    }
}
