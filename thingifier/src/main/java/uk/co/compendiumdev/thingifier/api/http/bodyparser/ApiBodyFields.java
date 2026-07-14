package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

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

    public ValidationReport validateAgainstType(final EntityDefinition entity) {
        return validateAgainstTypeIgnoring(entity, new ArrayList<>());
    }

    public ValidationReport validateAgainstTypeIgnoring(
            final EntityDefinition entity, final List<String> doNotValidateFields) {
        ValidationReport report = new ValidationReport();
        for (Map.Entry<String, Object> fieldValue : fields.entrySet()) {

            if (entity.hasAnyOfFieldNamesDefined(doNotValidateFields)) {
                continue;
            }

            Field field = entity.getField(fieldValue.getKey());
            if (field == null) {
                continue;
            }

            Object value = fieldValue.getValue();
            String instanceType = "Something Else";
            if (value instanceof String) {
                instanceType = "STRING";
            }
            if (value instanceof Boolean) {
                instanceType = "BOOLEAN";
            }
            if (value instanceof Integer) {
                instanceType = "INTEGER";
            }
            if (value instanceof Float) {
                instanceType = "NUMERIC";
            }
            if (value instanceof Double) {
                instanceType = "NUMERIC";
            }

            String errorMessage =
                    String.format(
                            "%s should be %s but was %s",
                            field.getName(), field.getType(), instanceType);

            if (field.getType() == FieldType.BOOLEAN && !(value instanceof Boolean)) {
                report.setValid(false);
                report.addErrorMessage(errorMessage);
            }
            if (field.getType() == FieldType.INTEGER
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                if (!(value instanceof Double)) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                } else {
                    fieldValue.setValue(((Double) value).intValue());
                }
            }
            if (field.getType() == FieldType.FLOAT && !(value instanceof Double)) {
                report.setValid(false);
                report.addErrorMessage(errorMessage);
            }
        }
        return report;
    }
}
