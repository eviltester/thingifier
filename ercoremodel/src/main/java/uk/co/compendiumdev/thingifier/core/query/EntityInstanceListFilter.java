package uk.co.compendiumdev.thingifier.core.query;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class EntityInstanceListFilter {
    EntityListFilterParamParser instanceFilter;

    /*
       Given a Map of
       FieldName,Value
       _sortBy,+-FieldName

    */
    public EntityInstanceListFilter(QueryFilterParams queryParams) {
        instanceFilter = new EntityListFilterParamParser(queryParams);
    }

    public EntityInstanceListFilter(final List<FilterBy> queryFilterParams) {
        instanceFilter = new EntityListFilterParamParser(queryFilterParams);
    }

    public List<EntityInstance> filter(final List<EntityInstance> foundItems) {

        List<EntityInstance> filtered = new ArrayList<>();

        for (EntityInstance instance : foundItems) {
            // does it match the filter?
            if (instanceFilter.matches(instance)) {
                filtered.add(instance);
            }
        }

        return filtered;
    }
}
