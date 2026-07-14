package uk.co.compendiumdev.thingifier.api.http.bodyparser;

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

            if (value instanceof Boolean) {
                stringsInMap.put(key, String.valueOf(value));
            }

            if (value instanceof String) {
                stringsInMap.put(key, (String) value);
            }

            if (value instanceof Double) {
                stringsInMap.put(key, String.valueOf(value));
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
        if (value instanceof String) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixKey, (String) value));
        }
        if (value instanceof Double) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixKey, String.valueOf(value)));
        }
        if (value instanceof Boolean) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixKey, String.valueOf(value)));
        }
        if (value instanceof Integer) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixKey, String.valueOf(value)));
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
        if (value instanceof ArrayList) {
            for (Object nestedValue : (ArrayList) value) {
                List<Map.Entry<String, String>> nestedValues =
                        flattenToStringMap(prefixKey + separator, nestedValue);
                stringsInMap.addAll(nestedValues);
            }
        }
        return stringsInMap;
    }

    private String stringValue(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Boolean || value instanceof Double || value instanceof Integer) {
            return String.valueOf(value);
        }
        return "";
    }

    private String sourceTypeName(final Object value) {
        if (value instanceof String) {
            return "STRING";
        }
        if (value instanceof Boolean) {
            return "BOOLEAN";
        }
        if (value instanceof Integer) {
            return "INTEGER";
        }
        if (value instanceof Float || value instanceof Double) {
            return "NUMERIC";
        }
        return "Something Else";
    }
}
