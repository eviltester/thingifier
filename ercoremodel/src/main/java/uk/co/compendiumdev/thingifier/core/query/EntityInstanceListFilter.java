package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

public class EntityInstanceListFilter {
    EntityListFilterParamParser instanceFilter;

    /*
        Given a Map of
        FieldName,Value
        sort_by,+-FieldName

     */
    public EntityInstanceListFilter(QueryFilterParams queryParams) {
        instanceFilter = new EntityListFilterParamParser(queryParams);
    }

    public EntityInstanceListFilter(final List<FilterBy> queryFilterParams) {
        instanceFilter = new EntityListFilterParamParser(queryFilterParams);
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
