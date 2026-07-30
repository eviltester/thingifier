package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;

public class QueryFilterParams {

    List<FilterBy> filterBys;

    public QueryFilterParams() {
        filterBys = new ArrayList<>();
    }

    public void put(String fieldName, String fieldValue) {
        filterBys.add(new FilterBy(fieldName, fieldValue));
    }

    public List<FilterBy> toList() {
        return filterBys;
    }

    public List<FilterBy> sortBys() {
        List<FilterBy> sortCriteria = new ArrayList<>();

        for (FilterBy by : filterBys) {
            if (SortByFieldName.isSortByParam(by.fieldName)) {
                sortCriteria.add(by);
            }
        }

        return sortCriteria;
    }

    public QueryFilterParams fieldFilters() {
        QueryFilterParams fieldFilters = new QueryFilterParams();

        for (FilterBy filterBy : filterBys) {
            if (!isReservedQueryControl(filterBy.fieldName)) {
                fieldFilters.add(
                        new FilterBy(
                                filterBy.fieldName,
                                filterBy.filterOperation + filterBy.fieldValue));
            }
        }

        return fieldFilters;
    }

    public QueryFilterParams withoutPagingParams() {
        QueryFilterParams params = new QueryFilterParams();

        for (FilterBy filterBy : filterBys) {
            if (!PaginationParams.isPaginationParam(filterBy.fieldName)) {
                params.add(
                        new FilterBy(
                                filterBy.fieldName,
                                filterBy.filterOperation + filterBy.fieldValue));
            }
        }

        return params;
    }

    public int size() {
        return filterBys.size();
    }

    public void add(FilterBy aFilterBy) {
        filterBys.add(aFilterBy);
    }

    public void addAll(final QueryFilterParams params) {
        if (params == null) {
            return;
        }

        for (FilterBy filterBy : params.toList()) {
            add(new FilterBy(filterBy.fieldName, filterBy.filterOperation + filterBy.fieldValue));
        }
    }

    public FilterBy get(int i) {
        return filterBys.get(i);
    }

    public boolean hasSortBy() {
        for (FilterBy filterBy : filterBys) {
            if (SortByFieldName.isSortByParam(filterBy.fieldName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isReservedQueryControl(final String fieldName) {
        return SortByFieldName.isSortByParam(fieldName)
                || PaginationParams.isPaginationParam(fieldName);
    }
}
