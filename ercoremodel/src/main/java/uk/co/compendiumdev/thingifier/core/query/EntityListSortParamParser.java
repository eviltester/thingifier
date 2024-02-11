package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: create a matchParams (like sortBys), and use an object to support longer term handling of complex filters like, ranges, etc.
public class EntityListSortParamParser {
    private final QueryFilterParams params;

    public EntityListSortParamParser(final QueryFilterParams queryParams) {
        this.params = queryParams;
    }

    public EntityListSortParamParser(final List<FilterBy> queryParams) {
        this.params = new QueryFilterParams();
        for(FilterBy filter : queryParams){
            if(isSortByParam(filter.fieldName)){
                this.params.put(filter.fieldName, filter.fieldValue);
            }
        }
    }

    /*
        return all the sortBy values
        currently sortBy=-FieldName or sortBy=+FieldName or sortBy=FieldName
        or sort_by=etc.

        TODO: handle multiple sort fields e.g. sortBy=-FieldName1,+FieldName2
     */
    public List<SortByFieldName> sortBys(){
        List<SortByFieldName>sortbys = new ArrayList<>();
        for(FilterBy field : params.sortBys()){
            if(isSortByParam(field.fieldName) ){
                final SortByFieldName aSortBy = new SortByFieldName();
                String sortByValue = field.fieldValue;
                switch (sortByValue.charAt(0)){
                    case '-':
                        aSortBy.order=1;
                        aSortBy.fieldName=sortByValue.substring(1).trim();
                        break;
                    case '+':
                        aSortBy.order=-1;
                        aSortBy.fieldName=sortByValue.substring(1).trim();
                        break;
                    default:
                        aSortBy.order=-1;
                        aSortBy.fieldName=sortByValue.trim();
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
