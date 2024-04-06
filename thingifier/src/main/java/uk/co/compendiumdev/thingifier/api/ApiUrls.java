package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class ApiUrls {
    private final ThingifierApiConfig apiConfig;

    public ApiUrls(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public String getCreatedLocationHeader(final EntityInstance thingInstance) {
        String entityPath;

        if (apiConfig.willUrlShowInstancesAsPlural()) {
            entityPath = thingInstance.getEntity().getPlural();
        } else {
            entityPath = thingInstance.getEntity().getName();
        }

        // use the primary key as the id
        String uniqueID = thingInstance.getPrimaryKeyValue();

        return apiConfig.getApiEndPointPrefix() + "/" + entityPath + "/" + uniqueID;
    }
}
