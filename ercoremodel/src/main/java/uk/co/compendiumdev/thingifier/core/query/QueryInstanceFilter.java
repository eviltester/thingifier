package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: EntityListFilterParamParser + EntityListSortParamParser
// TODO: create a matchParams (like sortBys), and use an object to support longer term handling of complex filters like, ranges, etc.
public class QueryInstanceFilter {
    private final Map<String, String> params;

    public QueryInstanceFilter(final Map<String, String> queryParams) {
        this.params = queryParams;
    }

    public boolean matches(final EntityInstance instance) {
        for(Map.Entry<String,String> field : params.entrySet()){

            final EntityDefinition defn = instance.getEntity();

            String fieldName = field.getKey();

            // TODO: handle <,>,>=, ranges, like, etc.
            if(defn.hasFieldNameDefined(fieldName)){
                if(!instance.getFieldValue(fieldName).asString().
                        equals(field.getValue())){
                    return false;
                }
            }
        }

        return true;
    }

    public class SortByFieldName{
        int order=1;
        String fieldName="";
    }

    /*
        return all the sortBy values
        currently sortBy=-FieldName or sortBy=+FieldName or sortBy=FieldName
        or sort_by=etc.

        TODO: handle multiple sort fields e.g. sortBy=-FieldName1,+FieldName2
     */
    public List<SortByFieldName> sortBys(){
        List<SortByFieldName>sortbys = new ArrayList<>();
        for(Map.Entry<String,String> field : params.entrySet()){
            if(field.getKey().equalsIgnoreCase("sortby") ||
                    field.getKey().equalsIgnoreCase("sort_by") ){
                final SortByFieldName aSortBy = new SortByFieldName();
                String sortByValue = field.getValue();
                switch (sortByValue.charAt(0)){
                    case '-':
                        aSortBy.order=-1;
                        aSortBy.fieldName=sortByValue.substring(1);
                        break;
                    case '+':
                        aSortBy.order=1;
                        aSortBy.fieldName=sortByValue.substring(1);
                        break;
                    default:
                        aSortBy.fieldName=sortByValue;
                        break;
                }
                sortbys.add(aSortBy);
            }
        }
        return sortbys;
    }
}
