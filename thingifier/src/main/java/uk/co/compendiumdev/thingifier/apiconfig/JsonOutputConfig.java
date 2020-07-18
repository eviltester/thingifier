package uk.co.compendiumdev.thingifier.apiconfig;

public class JsonOutputConfig {

    // TODO: have a default Response Accept format e.g. JSON, or XML
    private boolean allowCompressedRelationships;
    private Boolean jsonOutputRelationshipsUsesIdsIfAvailable;
    private boolean showGuidsInResponse;
    private boolean convertFieldsToDefinedTypes;

    public JsonOutputConfig(){
        allowCompressedRelationships=true;
        jsonOutputRelationshipsUsesIdsIfAvailable=true;
        showGuidsInResponse =true;
        convertFieldsToDefinedTypes =true;
    }

    public void setFrom(final JsonOutputConfig jsonOutput) {
        allowCompressedRelationships = jsonOutput.willRenderRelationshipsAsCompressed();
        jsonOutputRelationshipsUsesIdsIfAvailable = jsonOutput.willRenderRelationshipsWithIdsIfAvailable();
        showGuidsInResponse = jsonOutput.willRenderGuidsInResponse();
        convertFieldsToDefinedTypes = jsonOutput.willRenderFieldsAsDefinedTypes();
    }


    public void setCompressRelationships(final boolean config) {
        allowCompressedRelationships=config;
    }
    public void setRelationshipsUseIdsIfAvailable(final boolean config) {
        jsonOutputRelationshipsUsesIdsIfAvailable=config;
    }
    public void setConvertFieldsToDefinedTypes(final boolean config) {
        convertFieldsToDefinedTypes =config;
    }
    public void setShowGuidsInResponse(boolean config){
        showGuidsInResponse =config;
    }

    public Boolean willRenderRelationshipsAsCompressed() {
        return allowCompressedRelationships;
    }
    public Boolean willRenderRelationshipsWithIdsIfAvailable() {
        return jsonOutputRelationshipsUsesIdsIfAvailable;
    }
    public boolean willRenderGuidsInResponse() {
        return showGuidsInResponse;
    }
    public boolean willRenderFieldsAsDefinedTypes() {
        return convertFieldsToDefinedTypes;
    }

}
