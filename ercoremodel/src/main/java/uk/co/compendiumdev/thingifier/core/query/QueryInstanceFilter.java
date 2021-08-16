package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.Map;

public class QueryInstanceFilter {
    private final Map<String, String> params;

    public QueryInstanceFilter(final Map<String, String> queryParams) {
        this.params = queryParams;
    }

    public boolean matches(final EntityInstance instance) {
        for(Map.Entry<String,String> field : params.entrySet()){

            final EntityDefinition defn = instance.getEntity();

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
