package uk.co.compendiumdev.thingifier;

public class ThingifierApiConfig {

    private boolean willShowIdsInResponsesIfAvailable;
    private JsonOutputConfig jsonOutputConfig;
    private boolean willShowGuidsInResponses;
    private boolean willShowSingleInstancesAsPlural;
    private boolean willShowIdsInUrlsIfAvailable;

    public ThingifierApiConfig(){
        jsonOutputConfig = new JsonOutputConfig();

        // default to the most modern and 'up to date' config
        willShowSingleInstancesAsPlural=true;
        willShowGuidsInResponses=true;  // custom headers
        willShowIdsInResponsesIfAvailable = true; // relationship rendering
        willShowIdsInUrlsIfAvailable = true;  // location headers, api urls

        // by default
        jsonOutputConfig.allowShowGuidsInResponse(willShowGuidsInResponses);
        jsonOutputConfig.compressRelationships(true);
        jsonOutputConfig.relationshipsUsesIdsIfAvailable(willShowIdsInResponsesIfAvailable);

    }

    public JsonOutputConfig jsonOutput() {
        return jsonOutputConfig;
    }


    public ThingifierApiConfig allowShowGuidsInResponses(boolean allow){
        willShowGuidsInResponses = allow;
        if(!allow) {
            jsonOutput().relationshipsUsesIdsIfAvailable(true);
            jsonOutput().allowShowGuidsInResponse(false);
        }
        return this;
    }

    public ThingifierApiConfig allowShowIdsInResponsesIfAvailable(boolean allow) {
        willShowIdsInResponsesIfAvailable = allow;
        jsonOutput().relationshipsUsesIdsIfAvailable(allow);
        return this;
    }

    public ThingifierApiConfig allowShowIdsInUrlsIfAvailable(boolean allow) {
        willShowIdsInUrlsIfAvailable = allow;
        jsonOutput().relationshipsUsesIdsIfAvailable(allow);
        return this;
    }

    public ThingifierApiConfig showSingleInstancesAsPlural(boolean yes){
        willShowSingleInstancesAsPlural = yes;
        return this;
    }

    public boolean singleInstancesArePlural() {
        return willShowSingleInstancesAsPlural;
    }

    public boolean showIdsInResponsesIfAvailable() {
        return willShowIdsInResponsesIfAvailable;
    }

    public boolean showGuidsInResponses() {
        return willShowGuidsInResponses;
    }

    public boolean showIdsInUrlsIfAvailable() {
        return willShowIdsInUrlsIfAvailable;
    }
}
