package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.Thing;

public class RelationshipVector {

    private final String name;
    private final Cardinality cardinality;
    private Thing from;
    private Thing to;
    private RelationshipDefinition parentRelationship;

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

    public void addFromAndToFor(Thing from, Thing to, RelationshipDefinition relationshipDefinition) {
        this.from = from;
        this.to = to;
        this.parentRelationship = relationshipDefinition;
    }

    public Thing getTo() {
        return to;
    }

    public Thing getFrom() {
        return from;
    }

    public RelationshipDefinition getRelationshipDefinition() {
        return parentRelationship;
    }
}
