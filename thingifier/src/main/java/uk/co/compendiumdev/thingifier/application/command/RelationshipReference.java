package uk.co.compendiumdev.thingifier.application.command;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class RelationshipReference {

    private final String relationshipName;
    private final EntityDefinition targetEntity;
    private final String targetTerm;
    private final String referenceFieldName;
    private final String referenceValue;

    private RelationshipReference(
            final String relationshipName,
            final EntityDefinition targetEntity,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue) {
        this.relationshipName = relationshipName;
        this.targetEntity = targetEntity;
        this.targetTerm = targetTerm;
        this.referenceFieldName = referenceFieldName;
        this.referenceValue = referenceValue;
    }

    public static RelationshipReference compressed(
            final String relationshipName,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName, null, "", referenceFieldName, referenceValue);
    }

    public static RelationshipReference explicit(
            final String relationshipName,
            final EntityDefinition targetEntity,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName, targetEntity, targetTerm, referenceFieldName, referenceValue);
    }

    public String relationshipName() {
        return relationshipName;
    }

    public boolean hasExplicitTargetEntity() {
        return targetEntity != null;
    }

    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    public String targetTerm() {
        return targetTerm;
    }

    public String referenceFieldName() {
        return referenceFieldName;
    }

    public String referenceValue() {
        return referenceValue;
    }
}
