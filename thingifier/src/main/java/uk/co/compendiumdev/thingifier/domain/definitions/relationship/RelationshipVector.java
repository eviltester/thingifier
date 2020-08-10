package uk.co.compendiumdev.thingifier.domain.definitions.relationship;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.Cardinality;

import static uk.co.compendiumdev.thingifier.domain.definitions.relationship.Optionality.*;

public class RelationshipVector {

    private final String name;
    private final Cardinality cardinality;
    private Optionality optionality;
    private Thing from;
    private Thing to;
    private RelationshipDefinition parentRelationship;

    public RelationshipVector(String relationShipName, Cardinality cardinality) {
        this.name = relationShipName;
        this.cardinality = cardinality;
        this.optionality = OPTIONAL_RELATIONSHIP;
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

    public void setOptionality(final Optionality aGivenOptionality) {
        this.optionality = aGivenOptionality;
    }

    public Optionality getOptionality() {
        return optionality;
    }
}
