package uk.co.compendiumdev.thingifier;

public class JsonOutputConfig {

    private boolean allowCompressedRelationships;
    private Boolean jsonOutputRelationshipsUsesIdsIfAvailable;
    private boolean willShowGuidsInResponse;
    private boolean willConvertFieldsToDefinedTypes;

    public JsonOutputConfig(){
        allowCompressedRelationships=true;
        jsonOutputRelationshipsUsesIdsIfAvailable=true;
        willShowGuidsInResponse=true;
        willConvertFieldsToDefinedTypes=true;
    }


    public void setCompressRelationships(final boolean config) {
        allowCompressedRelationships=config;
    }
    public void setRelationshipsUseIdsIfAvailable(final boolean config) {
        jsonOutputRelationshipsUsesIdsIfAvailable=config;
    }
    public void setConvertFieldsToDefinedTypes(final boolean config) {
        willConvertFieldsToDefinedTypes=config;
    }
    public void setShowGuidsInResponse(boolean config){
        willShowGuidsInResponse=config;
    }

    public Boolean willRenderRelationshipsAsCompressed() {
        return allowCompressedRelationships;
    }
    public Boolean willRenderRelationshipsWithIdsIfAvailable() {
        return jsonOutputRelationshipsUsesIdsIfAvailable;
    }
    public boolean willRenderGuidsInResponse() {
        return willShowGuidsInResponse;
    }
    public boolean willRenderFieldsAsDefinedTypes() {
        return willConvertFieldsToDefinedTypes;
    }
}
