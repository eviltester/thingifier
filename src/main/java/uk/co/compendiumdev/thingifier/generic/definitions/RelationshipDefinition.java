package uk.co.compendiumdev.thingifier.generic.definitions;


import uk.co.compendiumdev.thingifier.Thing;

public class RelationshipDefinition {

    private final String name;
    private final Thing from;
    private final Thing to;
    private final Cardinality cardinality;



    public RelationshipDefinition(String relationShipName, Thing from, Thing to, Cardinality cardinality) {
        this.name = relationShipName;
        this.from = from;
        this.to = to;
        this.cardinality = cardinality;

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
