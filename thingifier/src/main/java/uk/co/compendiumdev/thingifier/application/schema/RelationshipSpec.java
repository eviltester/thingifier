package uk.co.compendiumdev.thingifier.application.schema;

public final class RelationshipSpec {

    private final String name;
    private final String fromEntityName;
    private final String toEntityName;

    public RelationshipSpec(
            final String name, final String fromEntityName, final String toEntityName) {
        this.name = name;
        this.fromEntityName = fromEntityName;
        this.toEntityName = toEntityName;
    }

    public String name() {
        return name;
    }

    public String fromEntityName() {
        return fromEntityName;
    }

    public String toEntityName() {
        return toEntityName;
    }
}
