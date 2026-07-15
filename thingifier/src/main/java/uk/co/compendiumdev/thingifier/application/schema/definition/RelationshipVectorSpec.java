package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class RelationshipVectorSpec {

    private final String name;
    private final CardinalitySpec cardinality;
    private final String optionality;

    public RelationshipVectorSpec(
            final String name, final CardinalitySpec cardinality, final String optionality) {
        this.name = name;
        this.cardinality = cardinality;
        this.optionality = optionality;
    }

    public String name() {
        return name;
    }

    public CardinalitySpec cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }
}
