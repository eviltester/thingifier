package uk.co.compendiumdev.thingifier.core.query;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class EntityInstanceListSorter {
    EntityListSortParamParser instanceFilter;

    /*
       Given a Map of
       FieldName,Value
       _sortBy,+-FieldName

    */
    public EntityInstanceListSorter(final QueryFilterParams queryParams) {
        instanceFilter = new EntityListSortParamParser(queryParams);
    }

    public List<EntityInstance> sort(final List<EntityInstance> foundItems) {

        List<EntityInstance> sorted = new ArrayList<>(foundItems);

        Comparator<EntityInstance> comparator = null;
        for (SortByFieldName sortBy : instanceFilter.sortBys()) {
            Comparator<EntityInstance> sortByField = compareByField(sortBy, sorted);
            if (sortByField != null) {
                comparator =
                        comparator == null ? sortByField : comparator.thenComparing(sortByField);
            }
        }

        if (comparator != null) {
            sorted.sort(comparator);
        }

        return sorted;
    }

    /** Sorted list of instances */
    public List<EntityInstance> sortByField(
            String fieldName, int order, final List<EntityInstance> itemsToSort) {

        List<EntityInstance> sortedList = new ArrayList<>();
        sortedList.addAll(itemsToSort);

        if (sortedList.size() == 0) {
            return sortedList;
        }

        Comparator<EntityInstance> compareByFieldValue =
                compareByField(fieldName, order, sortedList);
        if (compareByFieldValue == null) {
            return sortedList;
        }

        sortedList.sort(compareByFieldValue);

        return sortedList;
    }

    private Comparator<EntityInstance> compareByField(
            final SortByFieldName sortBy, final List<EntityInstance> itemsToSort) {
        return compareByField(sortBy.getFieldName(), sortBy.getOrder(), itemsToSort);
    }

    private Comparator<EntityInstance> compareByField(
            final String fieldName, final int order, final List<EntityInstance> itemsToSort) {
        if (itemsToSort.isEmpty()) {
            return null;
        }

        Field fieldDefn = itemsToSort.get(0).getEntity().getField(fieldName);
        if (fieldDefn == null) {
            return null;
        }

        Comparator<EntityInstance> compareByFieldValue =
                new Comparator<EntityInstance>() {
                    @Override
                    public int compare(EntityInstance thing1, EntityInstance thing2) {

                        final ComparableFieldValue comparableFieldValue1 =
                                new ComparableFieldValue(
                                        fieldDefn, thing1.getFieldValue(fieldName));
                        final ComparableFieldValue comparableFieldValue2 =
                                new ComparableFieldValue(
                                        fieldDefn, thing2.getFieldValue(fieldName));

                        return comparableFieldValue1.compareTo(comparableFieldValue2);
                    }
                };

        if (order < 0) {
            return compareByFieldValue;
        } else {
            return compareByFieldValue.reversed();
        }
    }
}
