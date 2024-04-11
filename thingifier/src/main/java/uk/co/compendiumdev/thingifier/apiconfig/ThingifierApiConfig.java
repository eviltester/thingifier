package uk.co.compendiumdev.thingifier.apiconfig;

public class ThingifierApiConfig {


    // the api endpoint url prefix e.g. /api for /api/todos or /apichallenges/api for /apichallenges/api/todos etc.
    private String apiEndpointPrefix;

    // Params
    // todo: willAllowFilteringThroughUrlParams  true/false (default: true)
    // todo: willEnforceFilteringThroughUrlParams true/false ie. 404 error if params when not supported (default: true)
    // todo: api request level allow filtering e.g. on some /things allow filtering but not others
    private final ParamConfig paramsConfig;


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

    // willApiEnforceAcceptHeaderForResponses i.e. respond with 406 if asked for but not allowed XML responses (default: true)
    boolean willApiEnforceAcceptHeaderForResponses;

    // willApiEnforceAcceptHeaderForResponses i.e. respond with 406 if asked for but not allowed XML responses (default: true)
    boolean willApiEnforceContentTypeHeaderForRequests;
    private boolean acceptXmlContent;
    private boolean acceptJsonContent;


    /*
        *** Responses ***
     */
    private final JsonOutputConfig jsonOutputConfig;
    // willShowFieldvaluesAsConverted Types e.g. boolean is `true` instead of "true"

    // willShowIdsIfAvailable e.g. instead of GUIDs everywhere, only use GUID if an ID not available
    private boolean willShowIdsInResponsesIfAvailable;

    // willShowGuidsInResponses e.g. if false then no GUIDs shown in responses at all
    private boolean willShowPrimaryKeyHeaderInResponses;

    private final AdminConfig adminConfig;


    // Requests
    // willEnforceDeclaredTypesInInput e.g. only accept if boolean is given as `true` indead of "true"
    // if not set to enforce then it will 'convert' as best it can prior to input
    // e.g. 91.2 would be a valid integer of value 91
    private boolean willEnforceDeclaredTypesInInput;

    // URLs
    // willUseIdsInUrlsIfAvailable - instead of GUIDs use Ids
    private boolean willShowIdsInUrlsIfAvailable;
    private final StatusCodeConfig statusCodeConfig;
    private boolean robotsCanIndexApiResponses;

    // allow the API to be configured to return single items as
    // collections
    private boolean returnSingleGetItemsAsCollection;
    private boolean supportsMultipleDatabases;

    // when no content type, assume it is JSON - otherwise reject as unknown
    private boolean defaultContentTypeAssumedToBeJson;

    public ThingifierApiConfig(String endPointPrefix){

        String prefixToUse = "";
        if(endPointPrefix!=null && !endPointPrefix.isEmpty()){
            if(!endPointPrefix.startsWith("/")){
                prefixToUse = "/";
            }
            prefixToUse = prefixToUse + endPointPrefix;
        }
        apiEndpointPrefix = prefixToUse;

        // default to the most modern and 'up to date' config
        willShowSingleInstancesAsPlural=true;
        willShowPrimaryKeyHeaderInResponses =true;  // custom headers
        willShowIdsInResponsesIfAvailable = true; // relationship rendering
        willShowIdsInUrlsIfAvailable = true;  // location headers, api urls
        willEnforceDeclaredTypesInInput = true;

        supportsMultipleDatabases=false; // single database by default

        defaultContentTypeAssumedToBeJson=false;

        willApiAllowXmlResponses = true;
        willApiAllowJsonResponses = true;
        willApiEnforceAcceptHeaderForResponses = true; // 406 if not supported header
        willApiEnforceContentTypeHeaderForRequests = true;
        acceptXmlContent = true;
        acceptJsonContent = true;
        robotsCanIndexApiResponses=true;

        // by default single items are not returned as a collection
        returnSingleGetItemsAsCollection = false;

        paramsConfig = new ParamConfig();

        // by default
        jsonOutputConfig = new JsonOutputConfig();
        jsonOutputConfig.setShowPrimaryKeyInResponse(willShowPrimaryKeyHeaderInResponses);
        jsonOutputConfig.setCompressRelationships(true);

        statusCodeConfig = new StatusCodeConfig();

        adminConfig = new AdminConfig();
    }

    public void setFrom(final ThingifierApiConfig apiConfig) {
        apiEndpointPrefix = apiConfig.apiEndpointPrefix;
        willShowSingleInstancesAsPlural=apiConfig.willUrlShowInstancesAsPlural();
        willShowPrimaryKeyHeaderInResponses =apiConfig.willResponsesShowPrimaryKeyHeader();
        willShowIdsInResponsesIfAvailable = apiConfig.willResponseShowIdsIfAvailable();
        willShowIdsInUrlsIfAvailable = apiConfig.willUrlsShowIdsIfAvailable();
        willEnforceDeclaredTypesInInput = apiConfig.willApiEnforceDeclaredTypesInInput();

        robotsCanIndexApiResponses = apiConfig.willAllowRobotsToIndexResponses();
        willApiAllowXmlResponses = apiConfig.willApiAllowXmlForResponses();
        willApiAllowJsonResponses = apiConfig.willApiAllowJsonForResponses();
        willApiEnforceAcceptHeaderForResponses = apiConfig.willApiEnforceAcceptHeaderForResponses();

        supportsMultipleDatabases = apiConfig.supportsMultipleDatabases();

        returnSingleGetItemsAsCollection = apiConfig.willReturnSingleGetItemsAsCollection();
        paramsConfig.setFrom(apiConfig.forParams());
        statusCodeConfig.setFrom(apiConfig.statusCodes());
        jsonOutputConfig.setFrom(apiConfig.jsonOutput());
        adminConfig.setFrom(apiConfig.adminConfig());
    }

    public AdminConfig adminConfig() {
        return adminConfig;
    }

    public JsonOutputConfig jsonOutput() {
        return jsonOutputConfig;
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

    public ThingifierApiConfig setApiToEnforceContentTypeForRequests(boolean config) {
        willApiEnforceContentTypeHeaderForRequests=config;
        return this;
    }

    public ThingifierApiConfig setApiToShowPrimaryKeyHeaderInResponse(boolean config) {
        willShowPrimaryKeyHeaderInResponses=config;
        return this;
    }

    public boolean willUrlShowInstancesAsPlural() {
        return willShowSingleInstancesAsPlural;
    }
    public boolean willResponseShowIdsIfAvailable() {
        return willShowIdsInResponsesIfAvailable;
    }
    public boolean willResponsesShowPrimaryKeyHeader() {
        return willShowPrimaryKeyHeaderInResponses;
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

    public boolean willApiEnforceContentTypeHeaderForRequests(){
        return willApiEnforceContentTypeHeaderForRequests;
    }

    public ParamConfig forParams() {
        return paramsConfig;
    }

    public StatusCodeConfig statusCodes() {
        return statusCodeConfig;
    }


    public boolean willAcceptXMLContent() {
        return acceptXmlContent;
    }

    public boolean willAcceptJSONContent() {
        return acceptJsonContent;
    }

    public ThingifierApiConfig setApiToAllowXmlForContentType(boolean allow) {
        acceptXmlContent = allow;
        return this;
    }

    public ThingifierApiConfig setApiToAllowJsonForContentType(boolean allow) {
        acceptJsonContent = allow;
        return this;
    }

    public ThingifierApiConfig setApiToAllowRobotsIndexingResponses(boolean allow) {
        robotsCanIndexApiResponses = allow;
        return this;
    }

    public boolean willPreventRobotsFromIndexingResponse() {
        return !robotsCanIndexApiResponses;
    }

    public boolean willAllowRobotsToIndexResponses() {
        return robotsCanIndexApiResponses;
    }

    public String getApiEndPointPrefix() {
        return apiEndpointPrefix;
    }

    public ThingifierApiConfig setReturnSingleGetItemsAsCollection(boolean allow){
        returnSingleGetItemsAsCollection = allow;
        return this;
    }
    public boolean willReturnSingleGetItemsAsCollection() {
        return returnSingleGetItemsAsCollection;
    }

    public boolean supportsMultipleDatabases() {
        return supportsMultipleDatabases;
    }

    public ThingifierApiConfig setSupportsMultipleDatabases(boolean allow){
        supportsMultipleDatabases = allow;
        return this;
    }

    public ThingifierApiConfig setDefaultContentTypeAsJson(boolean allow) {
        defaultContentTypeAssumedToBeJson=allow;
        return this;
    }

    public boolean willAllowJsonAsDefaultContentType() {
        return defaultContentTypeAssumedToBeJson;
    }
}
