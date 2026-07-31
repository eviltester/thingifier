package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ApiBodyFields {

    private final Map<String, Object> fields;

    private ApiBodyFields(final Map<String, Object> fields) {
        this.fields = new HashMap<>(fields);
    }

    public static ApiBodyFields empty() {
        return new ApiBodyFields(new HashMap<>());
    }

    public static ApiBodyFields fromMap(final Map<String, Object> fields) {
        if (fields == null) {
            return empty();
        }
        return new ApiBodyFields(fields);
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(fields);
    }

    public Map<String, String> asStringMap() {
        Map<String, String> stringsInMap = new HashMap<>();
        for (String key : fields.keySet()) {
            Object value = fields.get(key);

            if (isScalarValue(value)) {
                stringsInMap.put(key, stringValue(value));
            }
        }
        return stringsInMap;
    }

    public List<Map.Entry<String, String>> asFlattenedStringMap() {
        return flattenToStringMap("", fields);
    }

    public List<ApiBodyField> flattenedFields() {
        List<ApiBodyField> flattened = new ArrayList<>();
        for (Map.Entry<String, String> entry : asFlattenedStringMap()) {
            flattened.add(new ApiBodyField(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(flattened);
    }

    public List<ApiBodyField> topLevelFields() {
        List<ApiBodyField> topLevel = new ArrayList<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            topLevel.add(
                    new ApiBodyField(
                            entry.getKey(),
                            stringValue(entry.getValue()),
                            sourceTypeName(entry.getValue())));
        }
        return Collections.unmodifiableList(topLevel);
    }

    private List<Map.Entry<String, String>> flattenToStringMap(
            final String prefixKey, final Object value) {
        List<Map.Entry<String, String>> stringsInMap = new ArrayList<>();
        if (isScalarValue(value)) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixKey, stringValue(value)));
        }

        String separator = "";
        if (prefixKey != null && !prefixKey.isEmpty() && !prefixKey.endsWith(".")) {
            separator = ".";
        }
        if (value instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                List<Map.Entry<String, String>> nestedValues =
                        flattenToStringMap(
                                prefixKey + separator + entry.getKey(), entry.getValue());
                stringsInMap.addAll(nestedValues);
            }
        }
        if (value instanceof List) {
            for (Object nestedValue : (List) value) {
                List<Map.Entry<String, String>> nestedValues =
                        flattenToStringMap(prefixKey + separator, nestedValue);
                stringsInMap.addAll(nestedValues);
            }
        }
        return stringsInMap;
    }

    private static boolean isScalarValue(final Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number;
    }

    private static String stringValue(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        return "";
    }

    public static String sourceTypeNameFor(final Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            return "STRING";
        }
        if (value instanceof Boolean) {
            return "BOOLEAN";
        }
        if (isIntegralNumber(value)) {
            return "INTEGER";
        }
        if (isDecimalNumber(value)) {
            return "NUMERIC";
        }
        if (value instanceof Map) {
            return "OBJECT";
        }
        if (value instanceof List) {
            return "ARRAY";
        }
        return "Something Else";
    }

    private static boolean isIntegralNumber(final Object value) {
        return value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger;
    }

    private static boolean isDecimalNumber(final Object value) {
        return value instanceof Float || value instanceof Double || value instanceof BigDecimal;
    }

    private String sourceTypeName(final Object value) {
        return sourceTypeNameFor(value);
    }
}
