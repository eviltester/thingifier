package uk.co.compendiumdev.thingifier.application.schema;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public final class FieldSpec {

    private final String name;
    private final FieldType type;
    private final boolean unique;

    public FieldSpec(final String name, final FieldType type, final boolean unique) {
        this.name = name;
        this.type = type;
        this.unique = unique;
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
}
