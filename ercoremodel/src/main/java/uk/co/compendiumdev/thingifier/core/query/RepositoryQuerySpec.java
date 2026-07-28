package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class RepositoryQuerySpec {

    private final EntityDefinition entity;
    private final String identifier;
    private final String relationshipName;
    private final boolean singleTargetRelationshipsAsInstances;

    private RepositoryQuerySpec(
            final EntityDefinition entity,
            final String identifier,
            final String relationshipName,
            final boolean singleTargetRelationshipsAsInstances) {
        this.entity = entity;
        this.identifier = identifier;
        this.relationshipName = relationshipName;
        this.singleTargetRelationshipsAsInstances = singleTargetRelationshipsAsInstances;
    }

    public static RepositoryQuerySpec collection(final EntityDefinition entity) {
        return new RepositoryQuerySpec(entity, null, null, true);
    }

    public static RepositoryQuerySpec instance(
            final EntityDefinition entity, final String identifier) {
        return new RepositoryQuerySpec(entity, identifier, null, true);
    }

    public static RepositoryQuerySpec relationship(
            final EntityDefinition entity, final String identifier, final String relationshipName) {
        return relationship(entity, identifier, relationshipName, true);
    }

    public static RepositoryQuerySpec relationship(
            final EntityDefinition entity,
            final String identifier,
            final String relationshipName,
            final boolean singleTargetRelationshipsAsInstances) {
        return new RepositoryQuerySpec(
                entity, identifier, relationshipName, singleTargetRelationshipsAsInstances);
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

    public boolean singleTargetRelationshipsAsInstances() {
        return singleTargetRelationshipsAsInstances;
    }
}
