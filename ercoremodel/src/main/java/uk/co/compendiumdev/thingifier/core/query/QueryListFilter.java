package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryListFilter {
    private final Map<String, String> queryParams;
    QueryInstanceFilter instanceFilter;

    public QueryListFilter(final Map<String, String> queryParams) {
        this.queryParams = queryParams;
        instanceFilter = new QueryInstanceFilter(queryParams);
    }

    public List<EntityInstance> filter(final List<EntityInstance> foundItems) {

        List<EntityInstance> filtered = new ArrayList<>();

        for(EntityInstance instance : foundItems){
            // does it match the filter?
            if(instanceFilter.matches(instance)){
                filtered.add(instance);
            }
        }

        return filtered;
    }
}
