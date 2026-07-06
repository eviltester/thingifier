package uk.co.compendiumdev.thingifier.core.query;

public class SortByFieldName {
    int order = 1;
    String fieldName = "";

    public int getOrder() {
        return order;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static boolean isSortByParam(final String key) {
        return (key.equalsIgnoreCase("sortby") ||
                key.equalsIgnoreCase("sort_by"));
    }
}
