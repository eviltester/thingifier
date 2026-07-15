package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.Map;

public final class YamlRelationshipDocument {

    private final String fromEntityName;
    private final String name;
    private final String toEntityName;
    private final String cardinality;
    private final String optionality;
    private final YamlRelationshipReverseDocument reverse;

    private YamlRelationshipDocument(
            final String fromEntityName,
            final String name,
            final String toEntityName,
            final String cardinality,
            final String optionality,
            final YamlRelationshipReverseDocument reverse) {
        this.fromEntityName = fromEntityName;
        this.name = name;
        this.toEntityName = toEntityName;
        this.cardinality = cardinality;
        this.optionality = optionality;
        this.reverse = reverse;
    }

    static YamlRelationshipDocument fromObject(final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlRelationshipDocument(
                YamlMapSupport.stringValue(map.get("from")),
                YamlMapSupport.stringValue(map.get("name")),
                YamlMapSupport.stringValue(map.get("to")),
                YamlMapSupport.stringValue(map.get("cardinality")),
                YamlMapSupport.stringValue(map.get("optionality")),
                reverseFrom(map.get("reverse")));
    }

    public String fromEntityName() {
        return fromEntityName;
    }

    public String name() {
        return name;
    }

    public String toEntityName() {
        return toEntityName;
    }

    public String cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }

    public YamlRelationshipReverseDocument reverse() {
        return reverse;
    }

    private static YamlRelationshipReverseDocument reverseFrom(final Object source) {
        if (source == null) {
            return null;
        }
        return YamlRelationshipReverseDocument.fromObject(source);
    }
}
