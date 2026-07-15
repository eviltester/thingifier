package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlThingifierDocument {

    private final int formatVersion;
    private final String title;
    private final String description;
    private final Map<String, YamlEntityDocument> entities;
    private final List<YamlRelationshipDocument> relationships;

    private YamlThingifierDocument(
            final int formatVersion,
            final String title,
            final String description,
            final Map<String, YamlEntityDocument> entities,
            final List<YamlRelationshipDocument> relationships) {
        this.formatVersion = formatVersion;
        this.title = title;
        this.description = description;
        this.entities = entities;
        this.relationships = relationships;
    }

    public static YamlThingifierDocument fromObject(final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        final Map<String, Object> model = YamlMapSupport.asMap(map.get("model"));
        return new YamlThingifierDocument(
                formatVersionFrom(map.get("formatVersion")),
                YamlMapSupport.stringValue(model.get("title")),
                YamlMapSupport.stringValue(model.get("description")),
                entitiesFrom(map.get("entities")),
                relationshipsFrom(map.get("relationships")));
    }

    public int formatVersion() {
        return formatVersion;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Map<String, YamlEntityDocument> entities() {
        return entities;
    }

    public List<YamlRelationshipDocument> relationships() {
        return relationships;
    }

    private static int formatVersionFrom(final Object source) {
        final Integer value = YamlMapSupport.integerValue(source);
        return value == null ? -1 : value;
    }

    private static Map<String, YamlEntityDocument> entitiesFrom(final Object source) {
        final Map<String, YamlEntityDocument> entities = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : YamlMapSupport.stringMap(source).entrySet()) {
            entities.put(
                    entry.getKey(), YamlEntityDocument.fromEntry(entry.getKey(), entry.getValue()));
        }
        return entities;
    }

    private static List<YamlRelationshipDocument> relationshipsFrom(final Object source) {
        final List<YamlRelationshipDocument> relationships = new ArrayList<>();
        for (Object item : YamlMapSupport.listValue(source)) {
            relationships.add(YamlRelationshipDocument.fromObject(item));
        }
        return relationships;
    }
}
