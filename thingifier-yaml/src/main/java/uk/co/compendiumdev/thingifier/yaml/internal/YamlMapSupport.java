package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class YamlMapSupport {

    private YamlMapSupport() {
        // utility class
    }

    static Map<String, Object> asMap(final Object source) {
        if (source == null) {
            return new LinkedHashMap<>();
        }
        if (!(source instanceof Map)) {
            throw new IllegalArgumentException("Expected YAML object");
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    static Map<String, Object> stringMap(final Object source) {
        return asMap(source);
    }

    static List<Object> listValue(final Object source) {
        final List<Object> values = new ArrayList<>();
        if (source == null) {
            return values;
        }
        if (!(source instanceof List)) {
            throw new IllegalArgumentException("Expected YAML list");
        }
        values.addAll((List<?>) source);
        return values;
    }

    static List<String> stringList(final Object source) {
        final List<String> values = new ArrayList<>();
        for (Object value : listValue(source)) {
            values.add(stringValue(value));
        }
        return values;
    }

    static String stringValue(final Object source) {
        return source == null ? null : String.valueOf(source);
    }

    static boolean booleanValue(final Object source) {
        if (source instanceof Boolean) {
            return (Boolean) source;
        }
        return source != null && Boolean.parseBoolean(String.valueOf(source));
    }

    static Integer integerValue(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Number) {
            return ((Number) source).intValue();
        }
        return Integer.parseInt(String.valueOf(source));
    }
}
