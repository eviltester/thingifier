package uk.co.compendiumdev.thingifier.application.command;

public final class RelationshipReference {

    private enum Source {
        BODY,
        FIELD_BINDING
    }

    private final String relationshipName;
    private final String targetEntityName;
    private final String targetTerm;
    private final String referenceFieldName;
    private final String referenceValue;
    private final Source source;

    private RelationshipReference(
            final String relationshipName,
            final String targetEntityName,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue,
            final Source source) {
        this.relationshipName = relationshipName;
        this.targetEntityName = targetEntityName == null ? "" : targetEntityName;
        this.targetTerm = targetTerm == null ? "" : targetTerm;
        this.referenceFieldName = referenceFieldName;
        this.referenceValue = referenceValue;
        this.source = source;
    }

    /**
     * Creates a relationship reference from the compressed request body syntax.
     *
     * @param relationshipName relationship to connect
     * @param referenceFieldName field used to find the related instance
     * @param referenceValue value used to find the related instance
     * @return relationship reference parsed from request body data
     */
    public static RelationshipReference compressed(
            final String relationshipName,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName, "", "", referenceFieldName, referenceValue, Source.BODY);
    }

    /**
     * Creates a relationship reference from the explicit request body syntax.
     *
     * @param relationshipName relationship to connect
     * @param targetEntityName expected target entity name
     * @param targetTerm target term supplied by the request
     * @param referenceFieldName field used to find the related instance
     * @param referenceValue value used to find the related instance
     * @return relationship reference parsed from request body data
     */
    public static RelationshipReference explicit(
            final String relationshipName,
            final String targetEntityName,
            final String targetTerm,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName,
                targetEntityName,
                targetTerm,
                referenceFieldName,
                referenceValue,
                Source.BODY);
    }

    /**
     * Creates a relationship reference from a configured field binding.
     *
     * @param relationshipName relationship to connect or replace
     * @param targetEntityName target entity configured by the field
     * @param referenceFieldName field used to find the related instance
     * @param referenceValue value supplied in the source field
     * @return relationship reference derived from field metadata
     */
    public static RelationshipReference fieldBinding(
            final String relationshipName,
            final String targetEntityName,
            final String referenceFieldName,
            final String referenceValue) {
        return new RelationshipReference(
                relationshipName,
                targetEntityName,
                targetEntityName,
                referenceFieldName,
                referenceValue,
                Source.FIELD_BINDING);
    }

    /**
     * Returns the relationship name to connect.
     *
     * @return relationship name
     */
    public String relationshipName() {
        return relationshipName;
    }

    /**
     * Reports whether the reference specifies its target entity.
     *
     * @return true when target entity metadata is present
     */
    public boolean hasExplicitTargetEntity() {
        return !targetEntityName.isEmpty();
    }

    /**
     * Returns the explicit target entity name.
     *
     * @return target entity name, or an empty string when implicit
     */
    public String targetEntityName() {
        return targetEntityName;
    }

    /**
     * Returns the target term used in error messages.
     *
     * @return target term from the request or field binding
     */
    public String targetTerm() {
        return targetTerm;
    }

    /**
     * Returns the target field name used for lookup.
     *
     * @return reference field name
     */
    public String referenceFieldName() {
        return referenceFieldName;
    }

    /**
     * Returns the target field value used for lookup.
     *
     * @return reference value
     */
    public String referenceValue() {
        return referenceValue;
    }

    /**
     * Reports whether this reference was generated from field metadata.
     *
     * @return true when a normal field value should replace the bound relationship
     */
    public boolean isFieldBinding() {
        return source == Source.FIELD_BINDING;
    }
}
