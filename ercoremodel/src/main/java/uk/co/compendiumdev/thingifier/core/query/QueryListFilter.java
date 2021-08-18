package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

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

    public List<EntityInstance> sort(final List<EntityInstance> foundItems) {

        List<EntityInstance> sorted = new ArrayList<>(foundItems);

        for(QueryInstanceFilter.SortByFieldName sortBy :instanceFilter.sortBys()){
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

        if(fieldDefn==null)
            return sortedList;

        Comparator<EntityInstance> compareByFieldValue = new Comparator<EntityInstance>() {
            @Override
            public int compare(EntityInstance thing1, EntityInstance thing2) {

                if(     fieldDefn.getType() == FieldType.ID ||
                        fieldDefn.getType() == FieldType.INTEGER ){
                    int field1Value = thing1.getFieldValue(fieldName).asInteger();
                    int field2Value = thing2.getFieldValue(fieldName).asInteger();
                    return Integer.compare(field1Value, field2Value);
                }

                if( fieldDefn.getType() == FieldType.FLOAT){
                    float field1Value = thing1.getFieldValue(fieldName).asFloat();
                    float field2Value = thing2.getFieldValue(fieldName).asFloat();
                    return Float.compare(field1Value, field2Value);
                }

                if( fieldDefn.getType() == FieldType.BOOLEAN){
                    boolean field1Value = thing1.getFieldValue(fieldName).asBoolean();
                    boolean field2Value = thing2.getFieldValue(fieldName).asBoolean();
                    return Boolean.compare(field1Value, field2Value);
                }

                if( fieldDefn.getType() == FieldType.STRING){
                    String field1Value = thing1.getFieldValue(fieldName).asString();
                    String field2Value = thing2.getFieldValue(fieldName).asString();
                    return field1Value.compareTo(field2Value);
                }

                return 0;
            }
        };

        // low to high sort
        if(order>0) {
            Collections.sort(sortedList, compareByFieldValue.reversed());
        }else{
            Collections.sort(sortedList, compareByFieldValue);
        }

        return sortedList;
    }
}
