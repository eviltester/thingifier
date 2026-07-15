package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlEntityDocument {

    private final String name;
    private final String pluralName;
    private final Integer maxInstances;
    private final String primaryKey;
    private final Map<String, YamlFieldDocument> fields;

    private YamlEntityDocument(
            final String name,
            final String pluralName,
            final Integer maxInstances,
            final String primaryKey,
            final Map<String, YamlFieldDocument> fields) {
        this.name = name;
        this.pluralName = pluralName;
        this.maxInstances = maxInstances;
        this.primaryKey = primaryKey;
        this.fields = fields;
    }

    static YamlEntityDocument fromEntry(final String name, final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlEntityDocument(
                name,
                YamlMapSupport.stringValue(map.get("plural")),
                YamlMapSupport.integerValue(map.get("maxInstances")),
                YamlMapSupport.stringValue(map.get("primaryKey")),
                fieldsFrom(map.get("fields")));
    }

    public String name() {
        return name;
    }

    public String pluralName() {
        return pluralName;
    }

    public Integer maxInstances() {
        return maxInstances;
    }

    public String primaryKey() {
        return primaryKey;
    }

    public Map<String, YamlFieldDocument> fields() {
        return fields;
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
