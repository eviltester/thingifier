package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.Map;

public final class YamlRelationshipReverseDocument {

    private final String name;
    private final String cardinality;
    private final String optionality;

    private YamlRelationshipReverseDocument(
            final String name, final String cardinality, final String optionality) {
        this.name = name;
        this.cardinality = cardinality;
        this.optionality = optionality;
    }

    static YamlRelationshipReverseDocument fromObject(final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlRelationshipReverseDocument(
                YamlMapSupport.stringValue(map.get("name")),
                YamlMapSupport.stringValue(map.get("cardinality")),
                YamlMapSupport.stringValue(map.get("optionality")));
    }

    public String name() {
        return name;
    }

    public String cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }
}
