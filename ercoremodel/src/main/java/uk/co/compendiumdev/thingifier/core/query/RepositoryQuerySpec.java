package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class RepositoryQuerySpec {

    private final EntityDefinition entity;
    private final String identifier;
    private final String relationshipName;

    private RepositoryQuerySpec(
            final EntityDefinition entity, final String identifier, final String relationshipName) {
        this.entity = entity;
        this.identifier = identifier;
        this.relationshipName = relationshipName;
    }

    public static RepositoryQuerySpec collection(final EntityDefinition entity) {
        return new RepositoryQuerySpec(entity, null, null);
    }

    public static RepositoryQuerySpec instance(
            final EntityDefinition entity, final String identifier) {
        return new RepositoryQuerySpec(entity, identifier, null);
    }

    public static RepositoryQuerySpec relationship(
            final EntityDefinition entity, final String identifier, final String relationshipName) {
        return new RepositoryQuerySpec(entity, identifier, relationshipName);
    }

    public EntityDefinition entity() {
        return entity;
    }

    public String identifier() {
        return identifier;
    }

    public String relationshipName() {
        return relationshipName;
    }

    public boolean hasIdentifier() {
        return identifier != null;
    }

    public boolean hasRelationship() {
        return relationshipName != null;
    }
}
