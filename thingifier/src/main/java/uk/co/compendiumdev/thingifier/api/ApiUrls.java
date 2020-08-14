package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.List;

public class ApiUrls {
    private final ThingifierApiConfig apiConfig;

    public ApiUrls(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public String getCreatedLocationHeader(final ThingInstance thingInstance) {
        String uniqueID=null;
        String entityPath;

        if (apiConfig.willUrlShowInstancesAsPlural()) {
            entityPath = thingInstance.getEntity().getPlural();
        } else {
            entityPath = thingInstance.getEntity().getName();
        }

        // todo: entities should be able to specify specific fields as the 'id' regardless if it is an id or not
        // and this should be reflected in the REST API Urls
        if (apiConfig.willUrlsShowIdsIfAvailable()){
            final List<Field> idFields = thingInstance.getEntity().
                                            getFieldsOfType(FieldType.ID);
            if(!idFields.isEmpty()){
                uniqueID = thingInstance.getFieldValue(
                                idFields.get(0).getName()).asString();
            }
        }

        if(uniqueID==null){
            uniqueID = thingInstance.getGUID();
        }

        return entityPath + "/" + uniqueID;
    }
}
