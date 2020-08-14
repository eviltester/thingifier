package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Map;

public class QueryInstanceFilter {
    private final Map<String, String> params;

    public QueryInstanceFilter(final Map<String, String> queryParams) {
        this.params = queryParams;
    }

    public boolean matches(final ThingInstance instance) {
        for(Map.Entry<String,String> field : params.entrySet()){

            final ThingDefinition defn = instance.getEntity();

            String fieldName = field.getKey();

            if(defn.hasFieldNameDefined(fieldName)){
                if(!instance.getFieldValue(fieldName).asString().
                        equals(field.getValue())){
                    return false;
                }
            }
        }

        return true;
    }
}
