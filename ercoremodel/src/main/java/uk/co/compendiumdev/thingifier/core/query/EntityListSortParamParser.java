package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;

// TODO: create a matchParams (like sortBys), and use an object to support longer term handling of
// complex filters like, ranges, etc.
public class EntityListSortParamParser {
    private final QueryFilterParams params;

    public EntityListSortParamParser(final QueryFilterParams queryParams) {
        this.params = queryParams;
    }

    public EntityListSortParamParser(final List<FilterBy> queryParams) {
        this.params = new QueryFilterParams();
        for (FilterBy filter : queryParams) {
            if (isSortByParam(filter.fieldName)) {
                this.params.put(filter.fieldName, filter.fieldValue);
            }
        }
    }

    /*
       return all the _sortBy values
       currently _sortBy=-FieldName or _sortBy=+FieldName or _sortBy=FieldName
       or multiple sort fields e.g. _sortBy=-FieldName1,+FieldName2
    */
    public List<SortByFieldName> sortBys() {
        List<SortByFieldName> sortbys = new ArrayList<>();
        for (FilterBy field : params.sortBys()) {
            if (isSortByParam(field.fieldName)) {
                for (String sortByValue : field.fieldValue.split(",")) {
                    final SortByFieldName aSortBy = sortByFrom(sortByValue);
                    if (aSortBy != null) {
                        sortbys.add(aSortBy);
                    }
                }
            }
        }
        return sortbys;
    }

    private SortByFieldName sortByFrom(final String value) {
        String sortByValue = value.trim();
        if (sortByValue.isEmpty()) {
            return null;
        }

        final SortByFieldName aSortBy = new SortByFieldName();
        switch (sortByValue.charAt(0)) {
            case '-':
                aSortBy.order = 1;
                aSortBy.fieldName = sortByValue.substring(1).trim();
                break;
            case '+':
                aSortBy.order = -1;
                aSortBy.fieldName = sortByValue.substring(1).trim();
                break;
            default:
                aSortBy.order = -1;
                aSortBy.fieldName = sortByValue;
                break;
        }
        return aSortBy.fieldName.isEmpty() ? null : aSortBy;
    }

    public static boolean isSortByParam(final String key) {
        return SortByFieldName.isSortByParam(key);
    }
}
