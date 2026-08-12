package uk.co.compendiumdev.thingifier.core.query;

public class FilterBy {
    public final String fieldName;
    public final String fieldValue;
    public final FilterOperation filterOperation;

    public FilterBy(String key, String value) {
        fieldName = key;

        FilterOperation.ParsedValue parsedValue = FilterOperation.parseLeadingToken(value);
        fieldValue = parsedValue.value();
        filterOperation = parsedValue.operation();
    }

    public FilterBy(String key, FilterOperation operation, String value) {
        fieldName = key;
        filterOperation = operation == null ? FilterOperation.EQUALS : operation;
        fieldValue = value == null ? "" : value;
    }

    public FilterBy copy() {
        return new FilterBy(fieldName, filterOperation, fieldValue);
    }

    public String operationToken() {
        return filterOperation.token();
    }

    public String queryValue() {
        return operationToken() + fieldValue;
    }
}
