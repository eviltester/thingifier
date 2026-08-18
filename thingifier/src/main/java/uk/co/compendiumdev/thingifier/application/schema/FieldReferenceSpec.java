package uk.co.compendiumdev.thingifier.application.schema;

/**
 * Lightweight schema description of a field-backed relationship reference.
 *
 * <p>The field that owns this spec remains a normal value field. The spec tells request mapping
 * which target entity and field the value should resolve to, and which relationship should be
 * maintained from the source entity to that target.
 */
public final class FieldReferenceSpec {

    private final String targetEntityName;
    private final String targetFieldName;
    private final String relationshipName;

    /**
     * Creates a field reference description.
     *
     * @param targetEntityName entity containing the referenced value
     * @param targetFieldName field on the target entity to match
     * @param relationshipName relationship from the source entity to the target entity
     */
    public FieldReferenceSpec(
            final String targetEntityName,
            final String targetFieldName,
            final String relationshipName) {
        this.targetEntityName = targetEntityName;
        this.targetFieldName = targetFieldName;
        this.relationshipName = relationshipName;
    }

    /**
     * Returns the entity containing the referenced value.
     *
     * @return target entity name
     */
    public String targetEntityName() {
        return targetEntityName;
    }

    /**
     * Returns the target field name used for lookup.
     *
     * @return target field name
     */
    public String targetFieldName() {
        return targetFieldName;
    }

    /**
     * Returns the relationship maintained by this field.
     *
     * @return relationship name
     */
    public String relationshipName() {
        return relationshipName;
    }
}
