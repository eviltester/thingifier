package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Describes how a normal field value points at another entity through a named relationship.
 *
 * <p>The source field remains a persisted field on its own entity. Thingifier uses this metadata to
 * validate the referenced target exists and to maintain the configured relationship whenever the
 * source field is supplied in a write request.
 */
public final class FieldRelationshipReference {

    private final EntityDefinition targetEntity;
    private final String targetFieldName;
    private final String relationshipName;

    private FieldRelationshipReference(
            final EntityDefinition targetEntity,
            final String targetFieldName,
            final String relationshipName) {
        this.targetEntity = targetEntity;
        this.targetFieldName = requireText(targetFieldName, "targetFieldName");
        this.relationshipName = requireText(relationshipName, "relationshipName");
    }

    /**
     * Creates a field relationship reference.
     *
     * @param targetEntity entity containing the referenced value
     * @param targetFieldName field on the target entity to match
     * @param relationshipName relationship from the source entity to the target entity
     * @return relationship reference metadata
     */
    public static FieldRelationshipReference to(
            final EntityDefinition targetEntity,
            final String targetFieldName,
            final String relationshipName) {
        if (targetEntity == null) {
            throw new IllegalArgumentException("targetEntity is required");
        }
        return new FieldRelationshipReference(targetEntity, targetFieldName, relationshipName);
    }

    /**
     * Returns the entity containing the referenced value.
     *
     * @return target entity definition
     */
    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    /**
     * Returns the target field name used to resolve the relationship.
     *
     * @return target field name
     */
    public String targetFieldName() {
        return targetFieldName;
    }

    /**
     * Returns the relationship name to maintain from the source entity to the target entity.
     *
     * @return relationship name
     */
    public String relationshipName() {
        return relationshipName;
    }

    private static String requireText(final String value, final String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
