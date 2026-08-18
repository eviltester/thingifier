package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.Map;

public final class YamlRelationshipReverseDocument {

    private final String name;
    private final String cardinality;
    private final String optionality;
    private final boolean deleteTargetWhenDisconnected;
    private final boolean deleteTargetsWhenSourceDeleted;

    private YamlRelationshipReverseDocument(
            final String name,
            final String cardinality,
            final String optionality,
            final boolean deleteTargetWhenDisconnected,
            final boolean deleteTargetsWhenSourceDeleted) {
        this.name = name;
        this.cardinality = cardinality;
        this.optionality = optionality;
        this.deleteTargetWhenDisconnected = deleteTargetWhenDisconnected;
        this.deleteTargetsWhenSourceDeleted = deleteTargetsWhenSourceDeleted;
    }

    static YamlRelationshipReverseDocument fromObject(final Object source) {
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlRelationshipReverseDocument(
                YamlMapSupport.stringValue(map.get("name")),
                YamlMapSupport.stringValue(map.get("cardinality")),
                YamlMapSupport.stringValue(map.get("optionality")),
                YamlMapSupport.booleanValue(map.get("deleteTargetWhenDisconnected")),
                YamlMapSupport.booleanValue(map.get("deleteTargetsWhenSourceDeleted")));
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

    public boolean deleteTargetWhenDisconnected() {
        return deleteTargetWhenDisconnected;
    }

    public boolean deleteTargetsWhenSourceDeleted() {
        return deleteTargetsWhenSourceDeleted;
    }
}
