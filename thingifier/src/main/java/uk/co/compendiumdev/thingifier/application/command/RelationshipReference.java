package uk.co.compendiumdev.thingifier.application.command;

public final class RelationshipReference {

    private final String relationshipName;
    private final String targetEntityName;
    private final String targetTerm;
    private final String referenceFieldName;
    private final String referenceValue;

    private RelationshipReference(
            final String relationshipName,
            final String targetEntityName,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue) {
        this.relationshipName = relationshipName;
        this.targetEntityName = targetEntityName == null ? "" : targetEntityName;
        this.targetTerm = targetTerm == null ? "" : targetTerm;
        this.referenceFieldName = referenceFieldName;
        this.referenceValue = referenceValue;
    }

    public static RelationshipReference compressed(
            final String relationshipName,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName, "", "", referenceFieldName, referenceValue);
    }

    public static RelationshipReference explicit(
            final String relationshipName,
            final String targetEntityName,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName, targetEntityName, targetTerm, referenceFieldName, referenceValue);
    }

    public String relationshipName() {
        return relationshipName;
    }

    public boolean hasExplicitTargetEntity() {
        return !targetEntityName.isEmpty();
    }

    public String targetEntityName() {
        return targetEntityName;
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
