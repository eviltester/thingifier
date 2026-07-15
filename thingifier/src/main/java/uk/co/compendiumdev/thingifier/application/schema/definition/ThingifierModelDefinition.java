package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ThingifierModelDefinition {

    private final int formatVersion;
    private final String title;
    private final String description;
    private final List<EntityDefinitionSpec> entities;
    private final List<RelationshipDefinitionSpec> relationships;

    private ThingifierModelDefinition(final Builder builder) {
        formatVersion = builder.formatVersion;
        title = builder.title;
        description = builder.description;
        entities = Collections.unmodifiableList(new ArrayList<>(builder.entities));
        relationships = Collections.unmodifiableList(new ArrayList<>(builder.relationships));
    }

    public static Builder builder() {
        return new Builder();
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

    public List<EntityDefinitionSpec> entities() {
        return entities;
    }

    public List<RelationshipDefinitionSpec> relationships() {
        return relationships;
    }

    public EntityDefinitionSpec entityNamed(final String entityName) {
        for (EntityDefinitionSpec entity : entities) {
            if (entity.name().equals(entityName)) {
                return entity;
            }
        }
        return null;
    }

    public static final class Builder {

        private int formatVersion;
        private String title;
        private String description;
        private final List<EntityDefinitionSpec> entities;
        private final List<RelationshipDefinitionSpec> relationships;

        private Builder() {
            formatVersion = 1;
            entities = new ArrayList<>();
            relationships = new ArrayList<>();
        }

        public Builder formatVersion(final int formatVersion) {
            this.formatVersion = formatVersion;
            return this;
        }

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder entity(final EntityDefinitionSpec entity) {
            entities.add(entity);
            return this;
        }

        public Builder entities(final List<EntityDefinitionSpec> entities) {
            this.entities.addAll(entities);
            return this;
        }

        public Builder relationship(final RelationshipDefinitionSpec relationship) {
            relationships.add(relationship);
            return this;
        }

        public Builder relationships(final List<RelationshipDefinitionSpec> relationships) {
            this.relationships.addAll(relationships);
            return this;
        }

        public ThingifierModelDefinition build() {
            return new ThingifierModelDefinition(this);
        }
    }
}
