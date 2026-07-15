package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class RelationshipDefinitionSpec {

    private final String fromEntityName;
    private final String name;
    private final String toEntityName;
    private final CardinalitySpec cardinality;
    private final String optionality;
    private final RelationshipVectorSpec reverse;

    public RelationshipDefinitionSpec(
            final String fromEntityName,
            final String name,
            final String toEntityName,
            final CardinalitySpec cardinality,
            final String optionality,
            final RelationshipVectorSpec reverse) {
        this.fromEntityName = fromEntityName;
        this.name = name;
        this.toEntityName = toEntityName;
        this.cardinality = cardinality;
        this.optionality = optionality;
        this.reverse = reverse;
    }

    public String fromEntityName() {
        return fromEntityName;
    }

    public String name() {
        return name;
    }

    public String toEntityName() {
        return toEntityName;
    }

    public CardinalitySpec cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }

    public RelationshipVectorSpec reverse() {
        return reverse;
    }

    public boolean hasReverse() {
        return reverse != null;
    }
}
