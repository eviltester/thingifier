package uk.co.compendiumdev.thingifier.yaml.internal;

import java.util.Map;

public final class YamlFieldReferenceDocument {

    private final String targetEntityName;
    private final String targetFieldName;
    private final String relationshipName;

    private YamlFieldReferenceDocument(
            final String targetEntityName,
            final String targetFieldName,
            final String relationshipName) {
        this.targetEntityName = targetEntityName;
        this.targetFieldName = targetFieldName;
        this.relationshipName = relationshipName;
    }

    static YamlFieldReferenceDocument fromObject(final Object source) {
        if (source == null) {
            return null;
        }
        final Map<String, Object> map = YamlMapSupport.asMap(source);
        return new YamlFieldReferenceDocument(
                YamlMapSupport.stringValue(map.get("entity")),
                YamlMapSupport.stringValue(map.get("field")),
                YamlMapSupport.stringValue(map.get("relationship")));
    }

    public String targetEntityName() {
        return targetEntityName;
    }

    public String targetFieldName() {
        return targetFieldName;
    }

    public String relationshipName() {
        return relationshipName;
    }
}
