package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityDefinitionSpec {

    private final String name;
    private final String pluralName;
    private final int maxInstances;
    private final String primaryKeyFieldName;
    private final List<FieldDefinitionSpec> fields;

    private EntityDefinitionSpec(final Builder builder) {
        name = builder.name;
        pluralName = builder.pluralName;
        maxInstances = builder.maxInstances;
        primaryKeyFieldName = builder.primaryKeyFieldName;
        fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
    }

    public static Builder named(final String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public String pluralName() {
        return pluralName;
    }

    public int maxInstances() {
        return maxInstances;
    }

    public String primaryKeyFieldName() {
        return primaryKeyFieldName;
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyFieldName != null && !primaryKeyFieldName.trim().isEmpty();
    }

    public List<FieldDefinitionSpec> fields() {
        return fields;
    }

    public FieldDefinitionSpec fieldNamed(final String fieldName) {
        for (FieldDefinitionSpec field : fields) {
            if (field.name().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public static final class Builder {

        private final String name;
        private String pluralName;
        private int maxInstances;
        private String primaryKeyFieldName;
        private final List<FieldDefinitionSpec> fields;

        private Builder(final String name) {
            this.name = name;
            maxInstances = -1;
            fields = new ArrayList<>();
        }

        public Builder plural(final String pluralName) {
            this.pluralName = pluralName;
            return this;
        }

        public Builder maxInstances(final int maxInstances) {
            this.maxInstances = maxInstances;
            return this;
        }

        public Builder primaryKey(final String primaryKeyFieldName) {
            this.primaryKeyFieldName = primaryKeyFieldName;
            return this;
        }

        public Builder field(final FieldDefinitionSpec field) {
            fields.add(field);
            return this;
        }

        public Builder fields(final List<FieldDefinitionSpec> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public EntityDefinitionSpec build() {
            return new EntityDefinitionSpec(this);
        }
    }
}
