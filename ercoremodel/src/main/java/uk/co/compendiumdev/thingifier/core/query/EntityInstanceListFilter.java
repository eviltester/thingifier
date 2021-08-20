package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

// TODO: split into EntityInstanceListFilter and EntityInstanceListSorter
public class EntityInstanceListFilter {
    EntityListFilterParamParser instanceFilter;

    /*
        Given a Map of
        FieldName,Value
        sort_by,+-FieldName

     */
    public EntityInstanceListFilter(final Map<String, String> queryParams) {
        instanceFilter = new EntityListFilterParamParser(queryParams);
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
