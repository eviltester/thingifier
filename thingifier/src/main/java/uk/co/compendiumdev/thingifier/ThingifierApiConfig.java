package uk.co.compendiumdev.thingifier;

public class ThingifierApiConfig {

    private JsonOutputConfig jsonOutputConfig;

    public ThingifierApiConfig(){
        jsonOutputConfig = new JsonOutputConfig();
    }

    public JsonOutputConfig jsonOutput() {
        return jsonOutputConfig;
    }
}
