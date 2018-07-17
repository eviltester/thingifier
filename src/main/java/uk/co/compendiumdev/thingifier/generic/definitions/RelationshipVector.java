package uk.co.compendiumdev.thingifier.generic.definitions;

public class RelationshipVector {

    private final String name;
    private final Cardinality cardinality;

    public RelationshipVector(String relationShipName, Cardinality cardinality) {
        this.name = relationShipName;
        this.cardinality = cardinality;
    }

    public String getName() {
        return name;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
