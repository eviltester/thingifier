package uk.co.compendiumdev.thingifier;

public class JsonOutputConfig {

    private boolean allowCompressedRelationships;
    private Boolean jsonOutputRelationshipsUsesIdsIfAvailable;
    private boolean willShowGuidsInResponse;

    public JsonOutputConfig(){
        allowCompressedRelationships=true;
        jsonOutputRelationshipsUsesIdsIfAvailable=true;
        willShowGuidsInResponse=true;
    }

    public Boolean doesAllowCompressedRelationships() {
        return allowCompressedRelationships;
    }

    public Boolean doesRelationshipsUseIdsIfAvailable() {
        return jsonOutputRelationshipsUsesIdsIfAvailable;
    }

    public void compressRelationships(final boolean config) {
        allowCompressedRelationships=config;
    }

    public void relationshipsUsesIdsIfAvailable(final boolean config) {
        jsonOutputRelationshipsUsesIdsIfAvailable=config;
    }


    public void allowShowGuidsInResponse(boolean config){
        willShowGuidsInResponse=config;
    }
    public boolean showGuidsInResponse() {
        return willShowGuidsInResponse;
    }
}
