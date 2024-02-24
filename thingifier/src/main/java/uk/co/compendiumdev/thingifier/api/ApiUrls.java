package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

public class ApiUrls {
    private final ThingifierApiConfig apiConfig;

    public ApiUrls(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public String getCreatedLocationHeader(final EntityInstance thingInstance) {
        String uniqueID=null;
        String entityPath;

        if (apiConfig.willUrlShowInstancesAsPlural()) {
            entityPath = thingInstance.getEntity().getPlural();
        } else {
            entityPath = thingInstance.getEntity().getName();
        }

        // use the primary key as the id
        uniqueID = thingInstance.getPrimaryKeyValue();

        return entityPath + "/" + uniqueID;
    }
}
