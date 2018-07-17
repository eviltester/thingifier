package uk.co.compendiumdev.thingifier.generic.definitions;


import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;

public class RelationshipDefinition {

    /*
        A RelationshipDefinition defines the whole scope of the relationship
        Things will only know about the vectors that relate outwards from themselves.
     */
    private final String name;
    private final Thing from;
    private final Thing to;
    private final Cardinality cardinality;
    private RelationshipVector from_to;
    private RelationshipVector to_from;



    private RelationshipDefinition(Thing from, Thing to, RelationshipVector fromVector) {
        this.from = from;
        this.to = to;
        from_to = fromVector;
        fromVector.addFromAndToFor(from, to, this);
        this.name = fromVector.getName();
        this.cardinality = fromVector.getCardinality();
    }

    public static RelationshipDefinition create(Thing from, Thing to, RelationshipVector fromVector) {
        RelationshipDefinition defn = new RelationshipDefinition(from, to, fromVector);

        // and add the relationship Vector
        from.definition().addRelationship(fromVector);
        return defn;
    }

    public void whenReversed(Cardinality of, AndCall it) {
        withReverse(new RelationshipVector(it.isCalled(), of));
    }

    public RelationshipDefinition withReverse(RelationshipVector toVector){
        to_from = toVector;
        toVector.addFromAndToFor(to, from, this);
        to.definition().addRelationship(toVector);
        return this;
    }

    public String toString(){

        StringBuilder output = new StringBuilder();
        // TODO: the <=> should reflect the cardinality defined vectors created
        if(to_from==null) {
            output.append(String.format("\t\t%s : %s <=> %s %n",
                            this.name, from.definition().getName(), to.definition().getName()));
        }else{
            output.append(String.format("\t\t%1$s : %2$s <%1$s/%4$s> %3$s %n",
                    this.name, from.definition().getName(), to.definition().getName(), to_from.getName()));

        }

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


    public boolean isTwoWay() {
        return to_from!=null;
    }


    /*
        Return the representation of the reversal
     */
    public RelationshipVector getReversedRelationship() {
        return to_from;
    }

    /*
        A Relationship is known as any of its vector names
     */
    public boolean isKnownAs(String relationshipName) {

        if(from_to.getName().toLowerCase().equalsIgnoreCase(relationshipName)){
            return true;
        }
        if(to_from!=null && to_from.getName().toLowerCase().equalsIgnoreCase(relationshipName)){
            return true;
        }
        return false;
    }

    public RelationshipVector getFromRelationship() {
        return from_to;
    }
}
