package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlFieldDocument {

    private final String name;
    private final String type;
    private final boolean required;
    private final boolean unique;
    private final String defaultValue;
    private final String description;
    private final List<String> examples;
    private final Integer truncateTo;
    private final String minValue;
    private final String maxValue;
    private final List<Object> validations;
    private final Map<String, YamlFieldDocument> fields;
    private final YamlFieldReferenceDocument reference;

    private YamlFieldDocument(
            final String name,
            final String type,
            final boolean required,
            final boolean unique,
            final String defaultValue,
            final String description,
            final List<String> examples,
            final Integer truncateTo,
            final String minValue,
            final String maxValue,
            final List<Object> validations,
            final Map<String, YamlFieldDocument> fields,
            final YamlFieldReferenceDocument reference) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.unique = unique;
        this.defaultValue = defaultValue;
        this.description = description;
        this.examples = examples;
        this.truncateTo = truncateTo;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.validations = validations;
        this.fields = fields;
        this.reference = reference;
    }

    static YamlFieldDocument fromEntry(final String name, final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlFieldDocument(
                name,
                YamlMapSupport.stringValue(map.get("type")),
                YamlMapSupport.booleanValue(map.get("required")),
                YamlMapSupport.booleanValue(map.get("unique")),
                YamlMapSupport.stringValue(map.get("default")),
                YamlMapSupport.stringValue(map.get("description")),
                YamlMapSupport.stringList(map.get("examples")),
                YamlMapSupport.integerValue(map.get("truncateTo")),
                YamlMapSupport.stringValue(map.get("min")),
                YamlMapSupport.stringValue(map.get("max")),
                YamlMapSupport.listValue(map.get("validations")),
                fieldsFrom(map.get("fields")),
                YamlFieldReferenceDocument.fromObject(map.get("reference")));
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean required() {
        return required;
    }

    public boolean unique() {
        return unique;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public String description() {
        return description;
    }

    public List<String> examples() {
        return examples;
    }

    public Integer truncateTo() {
        return truncateTo;
    }

    public String minValue() {
        return minValue;
    }

    public String maxValue() {
        return maxValue;
    }

    public List<Object> validations() {
        return validations;
    }

    public Map<String, YamlFieldDocument> fields() {
        return fields;
    }

    public YamlFieldReferenceDocument reference() {
        return reference;
    }

    private static Map<String, YamlFieldDocument> fieldsFrom(final Object source) {
        final Map<String, YamlFieldDocument> fields = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : YamlMapSupport.stringMap(source).entrySet()) {
            fields.put(
                    entry.getKey(), YamlFieldDocument.fromEntry(entry.getKey(), entry.getValue()));
        }
        return fields;
    }
}
