package uk.co.compendiumdev.thingifier.application.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityTypeRef {

    private final String name;
    private final String pluralName;
    private final String primaryKeyFieldName;
    private final List<FieldSpec> fields;
    private final List<RelationshipSpec> relationships;

    public EntityTypeRef(
            final String name,
            final String pluralName,
            final String primaryKeyFieldName,
            final List<FieldSpec> fields,
            final List<RelationshipSpec> relationships) {
        this.name = name;
        this.pluralName = pluralName;
        this.primaryKeyFieldName = primaryKeyFieldName;
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
    }

    public String name() {
        return name;
    }

    public String pluralName() {
        return pluralName;
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyFieldName != null && !primaryKeyFieldName.isEmpty();
    }

    public String primaryKeyFieldName() {
        return primaryKeyFieldName;
    }

    public List<FieldSpec> fields() {
        return fields;
    }

    public List<RelationshipSpec> relationships() {
        return relationships;
    }

    public boolean hasRelationship(final String relationshipName) {
        return relationships.stream().anyMatch(spec -> spec.name().equals(relationshipName));
    }

    public FieldSpec fieldNamed(final String fieldName) {
        for (FieldSpec field : fields) {
            if (field.name().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
}
