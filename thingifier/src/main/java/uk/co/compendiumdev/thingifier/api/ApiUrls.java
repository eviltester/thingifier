package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class ApiUrls {
    private final ThingifierApiConfig apiConfig;

    public ApiUrls(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public String getCreatedLocationHeader(final ThingInstance thingInstance) {
        String uniqueID;
        String entityPath;

        if (apiConfig.singleInstancesArePlural()) {
            entityPath = thingInstance.getEntity().getPlural();
        } else {
            entityPath = thingInstance.getEntity().getName();
        }

        if (apiConfig.showIdsInUrlsIfAvailable() && thingInstance.hasIDField()){
            uniqueID = thingInstance.getID();
        }else{
            uniqueID = thingInstance.getGUID();
        }

        return entityPath + "/" + uniqueID;
    }
}
