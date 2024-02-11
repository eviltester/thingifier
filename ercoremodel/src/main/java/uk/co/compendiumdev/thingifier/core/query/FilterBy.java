package uk.co.compendiumdev.thingifier.core.query;

public class FilterBy {
    public String fieldName = "";
    public String fieldValue = "";
    public String filterOperation = "=";

    public static final String[] operators = {
            "<=", // <= e.g. ?id=<=2 id is less than or equal to 3
            ">=", // >= e.g. ?id=>=3 id is greater than or equal to 3
            "!=", // < e.g. ?id=!=3 id not equals 3
            "~=", // regex comparison match
            "*=", // wildcard comparison match
            "<", // < e.g. ?id=<3 id is less than 3
            ">", // < e.g. ?id=>3 id is > than 3
            "=", // < e.g. ?id==3 id equals 3
            "!", // < e.g. ?id=!3 id not equals 3
    };  // by default comparison filter will be equals e.g. ?id=3

    public FilterBy(String key, String value) {
        // key is the filename
        fieldName = key;

        // value might contain an operator
        String operatorToSet = "=";
        String valueToSet = value;

        for (String operator : operators) {
            if (value.startsWith(operator)) {
                operatorToSet = operator;
                valueToSet = value.substring(operator.length());
                break;
            }
        }

        fieldValue = valueToSet;
        filterOperation = operatorToSet;
    }
}
