package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: split into EntityListFilterParamParser + EntityListSortParamParser
// TODO: create a matchParams (like sortBys), and use an object to support longer term handling of complex filters like, ranges, etc.
public class EntityListSortParamParser {
    private final Map<String, String> params;

    public EntityListSortParamParser(final Map<String, String> queryParams) {
        this.params = queryParams;
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
            if(isSortByParam(field.getKey()) ){
                final SortByFieldName aSortBy = new SortByFieldName();
                String sortByValue = field.getValue();
                switch (sortByValue.charAt(0)){
                    case '-':
                        aSortBy.order=1;
                        aSortBy.fieldName=sortByValue.substring(1);
                        break;
                    case '+':
                        aSortBy.order=-1;
                        aSortBy.fieldName=sortByValue.substring(1);
                        break;
                    default:
                        aSortBy.order=-1;
                        aSortBy.fieldName=sortByValue;
                        break;
                }
                sortbys.add(aSortBy);
            }
        }
        return sortbys;
    }

    public static boolean isSortByParam(final String key) {
        return (key.equalsIgnoreCase("sortby") ||
                key.equalsIgnoreCase("sort_by"));
    }
}
