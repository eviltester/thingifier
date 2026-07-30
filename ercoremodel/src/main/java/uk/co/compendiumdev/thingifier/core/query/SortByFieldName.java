package uk.co.compendiumdev.thingifier.core.query;

public class SortByFieldName {
    public static final String PARAMETER_NAME = "_sortBy";

    int order = 1;
    String fieldName = "";

    public int getOrder() {
        return order;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static boolean isSortByParam(final String key) {
        return PARAMETER_NAME.equals(key);
    }
}
