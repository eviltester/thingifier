package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityListFilterParamParser {
    private final Map<String, String> params;
    private final List<FilterBy> filterByConditions;

    public EntityListFilterParamParser(final Map<String, String> queryParams) {
        this.params = queryParams;
        this.filterByConditions = filterBys();
    }

    public boolean matches(final EntityInstance instance) {
        for(FilterBy filterByCondition : filterByConditions){

            final EntityDefinition defn = instance.getEntity();

            String fieldName = filterByCondition.fieldName;

            // TODO: handle <,>,>=, ranges, like, etc.
            if(defn.hasFieldNameDefined(fieldName)){
                String value = instance.getFieldValue(fieldName).asString();
                switch (filterByCondition.filterOperation){
                    case "=":
                        if(!value.equals(filterByCondition.fieldValue)){
                            return false;
                        }
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
    }

    public List<FilterBy> filterBys(){

        List<FilterBy>filterbys = new ArrayList<>();

        for(Map.Entry<String,String> field : params.entrySet()) {
            if (!EntityListSortParamParser.isSortByParam(field.getKey())) {

                FilterBy filterby = new FilterBy();
                filterby.fieldName = field.getKey();
                filterby.filterOperation = "=";
                filterby.fieldValue = field.getValue();
                filterbys.add(filterby);
            }
        }

        return filterbys;
    }


}
