package uk.co.compendiumdev.thingifier.apiconfig;

public class JsonOutputConfig {

    // TODO: have a default Response Accept format e.g. JSON, or XML
    private boolean allowCompressedRelationships;
    private boolean convertFieldsToDefinedTypes;
    private boolean showPrimaryKeyHeaderInResponse;

    public JsonOutputConfig(){
        allowCompressedRelationships=true;
        convertFieldsToDefinedTypes =true;
        showPrimaryKeyHeaderInResponse=true;
    }

    public void setFrom(final JsonOutputConfig jsonOutput) {
        allowCompressedRelationships = jsonOutput.willRenderRelationshipsAsCompressed();
        convertFieldsToDefinedTypes = jsonOutput.willRenderFieldsAsDefinedTypes();
        showPrimaryKeyHeaderInResponse = jsonOutput.willShowPrimaryKeyHeaderInResponse();
    }

    private boolean willShowPrimaryKeyHeaderInResponse() {
        return showPrimaryKeyHeaderInResponse;
    }


    public void setCompressRelationships(final boolean config) {
        allowCompressedRelationships=config;
    }
    public void setConvertFieldsToDefinedTypes(final boolean config) {
        convertFieldsToDefinedTypes =config;
    }
    public void setShowPrimaryKeyInResponse(boolean config){
        showPrimaryKeyHeaderInResponse =config;
    }

    public Boolean willRenderRelationshipsAsCompressed() {
        return allowCompressedRelationships;
    }

    public boolean willRenderFieldsAsDefinedTypes() {
        return convertFieldsToDefinedTypes;
    }

}
