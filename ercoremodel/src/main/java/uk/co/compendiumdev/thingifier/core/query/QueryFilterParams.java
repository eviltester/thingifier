package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryFilterParams {

    List<FilterBy> filterBys;

    public QueryFilterParams(){
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

        for(FilterBy by : filterBys){
            if(SortByFieldName.isSortByParam(by.fieldName)){
                sortCriteria.add(by);
            }
        }

        return sortCriteria;
    }

    public int size() {
        return filterBys.size();
    }

    public void add(FilterBy aFilterBy) {
        filterBys.add(aFilterBy);
    }

    public FilterBy get(int i) {
        return filterBys.get(i);
    }

    public boolean hasSortBy() {
        for(FilterBy filterBy : filterBys){
            if(filterBy.fieldName.equals("sortBy") || filterBy.fieldName.equals("sort_by")){
                return true;
            }
        }

        return false;
    }
}
