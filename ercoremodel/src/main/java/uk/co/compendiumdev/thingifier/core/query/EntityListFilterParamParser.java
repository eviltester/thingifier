package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityListFilterParamParser {

    private final List<FilterBy> filterByConditions;

    public EntityListFilterParamParser(final Map<String, String> queryParams) {
        // because  a map is used to set this up we can't handle multiple conditions
        // TODO: the combo field would need to be configurable to allow entities
        // to have a field called comoband
        // need a different representation for combinations e.g. comboand=[id<1,id>10]
        this.filterByConditions = paramsMapToList(queryParams);
    }

    public EntityListFilterParamParser(final List<FilterBy> queryParams) {
        this.filterByConditions = queryParams;
    }

    public boolean matches(final EntityInstance instance) {
        for(FilterBy filterByCondition : filterByConditions){

            final EntityDefinition defn = instance.getEntity();

            String fieldName = filterByCondition.fieldName;

            // TODO: handle - ranges, like, or etc.
            // currently all conditions are treated as an AND clause e.g. ?ID=<10&ID=>5  would be is 6, 7, 8, 9
            if(defn.hasFieldNameDefined(fieldName)){
                String value = instance.getFieldValue(fieldName).asString();
                // get the actual value
                final ComparableFieldValue actualValue = new ComparableFieldValue(defn.getField(fieldName), instance.getFieldValue(fieldName));
                // create a comparison value
                final ComparableFieldValue filterConditionValue = new ComparableFieldValue(defn.getField(fieldName), FieldValue.is(fieldName, filterByCondition.fieldValue));

                switch (filterByCondition.filterOperation){
                    case "=":
                        if(!(actualValue.compareTo(filterConditionValue)==0)){
                            return false;
                        }
                        break;
                    case "<":
                        if(!(actualValue.compareTo(filterConditionValue)<0)){
                            return false;
                        }
                        break;
                    case ">":
                        if(!(actualValue.compareTo(filterConditionValue)>0)){
                            return false;
                        }
                        break;
                    case "<=":
                        if(!(actualValue.compareTo(filterConditionValue)<=0)){
                            return false;
                        }
                        break;
                    case ">=":
                        if(!(actualValue.compareTo(filterConditionValue)>=0)){
                            return false;
                        }
                        break;
                    case "!=":
                    case "!":
                        if(!(actualValue.compareTo(filterConditionValue)!=0)){
                            return false;
                        }
                        break;
                    default:
                        System.out.println(String.format("Unhandled filterby condition %s%s%s",
                                            fieldName, filterByCondition.filterOperation, value
                                ));
                }
            }
        }

        return true;
    }

    public List<FilterBy> filterBys(){
        return filterByConditions;
    }

    private List<FilterBy> paramsMapToList(Map<String,String> params){
        List<FilterBy>filterbys = new ArrayList<>();

        try {
            for (Map.Entry<String, String> field : params.entrySet()) {
                if (!EntityListSortParamParser.isSortByParam(field.getKey())) {

                    FilterBy filterby = new FilterBy(field.getKey(), field.getValue());
                    filterbys.add(filterby);
                }
            }
        }catch(Exception e){
            System.out.println("Error parsing params map to filter bys");
            System.out.println(e.getMessage());
        }

        return filterbys;
    }

}
