package uk.co.compendiumdev.thingifier.application.schema;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public final class FieldSpec {

    private final String name;
    private final FieldType type;
    private final boolean unique;
    private final FieldReferenceSpec reference;

    public FieldSpec(final String name, final FieldType type, final boolean unique) {
        this(name, type, unique, null);
    }

    public FieldSpec(
            final String name,
            final FieldType type,
            final boolean unique,
            final FieldReferenceSpec reference) {
        this.name = name;
        this.type = type;
        this.unique = unique;
        this.reference = reference;
    }

    public String name() {
        return name;
    }

    public FieldType type() {
        return type;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isProtectedField() {
        return type == FieldType.AUTO_INCREMENT || type == FieldType.AUTO_GUID;
    }

    /**
     * Reports whether this field is bound to a relationship reference.
     *
     * @return true when write mapping should also create a relationship reference
     */
    public boolean hasRelationshipReference() {
        return reference != null;
    }

    /**
     * Returns the field-backed relationship reference metadata.
     *
     * @return relationship reference spec, or null when none is configured
     */
    public FieldReferenceSpec relationshipReference() {
        return reference;
    }
}
