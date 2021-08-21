package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

public class EntityListFilterParamParser {
    private final Map<String, String> params;
    private final List<FilterBy> filterByConditions;

    public EntityListFilterParamParser(final Map<String, String> queryParams) {
        // TODO: because  a map is used to set this up we can't handle multiple conditions
        // TODO: the combo field would need to be configurable to allow entities
        // to have a field called comoband
        // need a different representation for combinations e.g. comboand=[id<1,id>10]
        this.params = queryParams;
        this.filterByConditions = filterBys();
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

    public class FilterBy{
        String fieldName="";
        String fieldValue="";
        String filterOperation="=";

        public FilterBy(String key, String value){
            // key is the filename
            fieldName=key;

            // value might contain an operator
            String []operators = {  "<=", // <= e.g. ?id=<=2 id is less than or equal to 3
                                    ">=", // >= e.g. ?id=>=3 id is greater than or equal to 3
                                    "<", // < e.g. ?id=<3 id is less than 3
                                    ">", // < e.g. ?id=>3 id is > than 3
                                    "=", // < e.g. ?id==3 id equals 3
                                    "!", // < e.g. ?id=!3 id not equals 3
                                    "!=" // < e.g. ?id=!=3 id not equals 3
                                };  // by default comparison filter will be equals e.g. ?id=3

            String operatorToSet = "=";
            String valueToSet = value;

            for(String operator : operators) {
                if (value.startsWith(operator)){
                    operatorToSet=operator;
                    valueToSet = value.substring(operator.length());
                    break;
                }
            }

            fieldValue = valueToSet;
            filterOperation = operatorToSet;
        }
    }

    public List<FilterBy> filterBys(){

        List<FilterBy>filterbys = new ArrayList<>();

        for(Map.Entry<String,String> field : params.entrySet()) {
            if (!EntityListSortParamParser.isSortByParam(field.getKey())) {

                FilterBy filterby = new FilterBy(field.getKey(), field.getValue());
                filterbys.add(filterby);
            }
        }

        return filterbys;
    }


}
