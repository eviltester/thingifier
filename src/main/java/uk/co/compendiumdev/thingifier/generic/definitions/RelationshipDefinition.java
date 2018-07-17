package uk.co.compendiumdev.thingifier.generic.definitions;


import uk.co.compendiumdev.thingifier.Thing;

public class RelationshipDefinition {

    private final String name;
    private final Thing from;
    private final Thing to;
    private final Cardinality cardinality;
    private RelationshipVector from_to;
    private RelationshipVector to_from;



    public RelationshipDefinition(Thing from, Thing to, RelationshipVector fromVector) {
        this.from = from;
        this.to = to;
        from_to = fromVector;
        this.name = fromVector.getName();
        this.cardinality = fromVector.getCardinality();
    }

    public RelationshipDefinition withReverse(RelationshipVector toVector){
        to_from = toVector;
        return this;
    }

    public String toString(){

        StringBuilder output = new StringBuilder();
        // TODO: the <=> should reflect the cardinality defined
        output.append("\t\t" + this.name + " : " + from.definition().getName() + " <=> " + to.definition().getName() + "\n");

        return output.toString();
    }

    public String getName() {
        return name;
    }

    public ThingDefinition to() {
        return to.definition();
    }

    public Thing from() {
        return from;
    }
}
