package uk.co.compendiumdev.thingifier.domain.definitions;


import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.dsl.relationship.AndCall;

public class RelationshipDefinition {

    /*
        A RelationshipDefinition defines the whole scope of the relationship
        Things will only know about the vectors that relate outwards from themselves.
     */
    private final String name;
    private final Thing from;
    private final Thing to;
    private RelationshipVector fromTo;
    private RelationshipVector toFrom;


    private RelationshipDefinition(Thing from, Thing to, RelationshipVector fromVector) {
        this.from = from;
        this.to = to;
        fromTo = fromVector;
        fromVector.addFromAndToFor(from, to, this);
        this.name = fromVector.getName();
    }

    public static RelationshipDefinition create(Thing from, Thing to, RelationshipVector fromVector) {
        RelationshipDefinition defn = new RelationshipDefinition(from, to, fromVector);

        // and add the relationship Vector
        from.definition().addRelationship(fromVector);
        return defn;
    }

    public RelationshipDefinition whenReversed(Cardinality of, AndCall it) {
        withReverse(new RelationshipVector(it.isCalled(), of));
        return this;
    }

    public RelationshipDefinition withReverse(RelationshipVector toVector) {
        toFrom = toVector;
        toVector.addFromAndToFor(to, from, this);
        to.definition().addRelationship(toVector);
        return this;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();
        // TODO: the <=> should reflect the cardinality defined vectors created
        if (toFrom == null) {
            output.append(String.format("\t\t%s : %s <=> %s %n",
                    this.name, from.definition().getName(), to.definition().getName()));
        } else {
            output.append(String.format("\t\t%1$s : %2$s <%1$s/%4$s> %3$s %n",
                    this.name, from.definition().getName(), to.definition().getName(), toFrom.getName()));

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
        return toFrom != null;
    }


    /*
        Return the representation of the reversal
     */
    public RelationshipVector getReversedRelationship() {
        return toFrom;
    }

    /*
        A Relationship is known as any of its vector names
     */
    public boolean isKnownAs(String relationshipName) {

        if (fromTo.getName().toLowerCase().equalsIgnoreCase(relationshipName)) {
            return true;
        }
        if (toFrom != null && toFrom.getName().toLowerCase().equalsIgnoreCase(relationshipName)) {
            return true;
        }
        return false;
    }

    public RelationshipVector getFromRelationship() {
        return fromTo;
    }


    // TODO - add during creation, not afterwards
    public RelationshipDefinition hasOptionality(final String fromOptionality, final String toOptionality) {
        // from optionality is from to e.g. from must have to "M", or from can have to "O"

        fromTo.setOptionality( fromOptionality.equalsIgnoreCase("M") ? Optionality.MANDATORY_RELATIONSHIP : Optionality.OPTIONAL_RELATIONSHIP);
        if(toFrom!=null) {
            toFrom.setOptionality(toOptionality.equalsIgnoreCase("M") ? Optionality.MANDATORY_RELATIONSHIP : Optionality.OPTIONAL_RELATIONSHIP);
        }

        return this;
    }

    public Optionality getOptionalityTo() {
        return toFrom.getOptionality();
    }

    public Optionality getOptionalityFrom() {
        return fromTo.getOptionality();
    }
}
