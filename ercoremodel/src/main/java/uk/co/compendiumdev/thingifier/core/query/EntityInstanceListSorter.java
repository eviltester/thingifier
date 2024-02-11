package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

public class EntityInstanceListSorter {
    EntityListSortParamParser instanceFilter;

    /*
        Given a Map of
        FieldName,Value
        sort_by,+-FieldName

     */
    public EntityInstanceListSorter(final QueryFilterParams queryParams) {
        instanceFilter = new EntityListSortParamParser(queryParams);
    }

    public List<EntityInstance> sort(final List<EntityInstance> foundItems) {

        List<EntityInstance> sorted = new ArrayList<>(foundItems);

        for(SortByFieldName sortBy :instanceFilter.sortBys()){
            sorted = sortByField(sortBy.fieldName, sortBy.order, sorted);
        }

        return sorted;
    }

    /**
     * Sorted list of instances
     */

    // TODO: unit tests for sorting
    public List<EntityInstance> sortByField(String fieldName, int order, final List<EntityInstance> itemsToSort) {

        List<EntityInstance>sortedList = new ArrayList<>();
        sortedList.addAll(itemsToSort);

        if(sortedList.size()==0)
            return sortedList;

        Field fieldDefn = sortedList.get(0).getEntity().getField(fieldName);

        // there is no field of that name
        if(fieldDefn==null)
            return sortedList;

        Comparator<EntityInstance> compareByFieldValue = new Comparator<EntityInstance>() {
            @Override
            public int compare(EntityInstance thing1, EntityInstance thing2) {

                final ComparableFieldValue comparableFieldValue1 = new ComparableFieldValue(fieldDefn, thing1.getFieldValue(fieldName));
                final ComparableFieldValue comparableFieldValue2 = new ComparableFieldValue(fieldDefn, thing2.getFieldValue(fieldName));

                return comparableFieldValue1.compareTo(comparableFieldValue2);
            }
        };


        if(order<0) {
            // (desc)
            Collections.sort(sortedList, compareByFieldValue);
        }else{
            // low to high sort (asc)
            Collections.sort(sortedList, compareByFieldValue.reversed());
        }

        return sortedList;
    }
}
