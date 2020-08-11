package uk.co.compendiumdev.thingifier.domain.definitions.relationship;


import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.dsl.relationship.AndCall;

public class RelationshipDefinition {

    /*
        A RelationshipDefinition defines the whole scope of the relationship
        Things will only know about the specific Relationship vectors that relate outwards from themselves.
     */
    private final String name;
    private final Thing from;
    private final Thing to;
    private RelationshipVector fromTo;
    private RelationshipVector toFrom;


    private RelationshipDefinition(RelationshipVector fromVector) {
        this.from = fromVector.getFrom();
        this.to = fromVector.getTo();
        fromTo = fromVector;
        fromVector.forRelationship(this);
        this.name = fromVector.getName();
    }

    public static RelationshipDefinition create(RelationshipVector fromVector) {
        RelationshipDefinition defn = new RelationshipDefinition(fromVector);
        // and add the relationship Vector to the from thing
        defn.from().definition().related().addRelationship(fromVector);
        return defn;
    }

    public RelationshipDefinition whenReversed(Cardinality of, AndCall it) {
        final RelationshipVector vector = new RelationshipVector(this.to, it.isCalled(), this.from, of);
        vector.forRelationship(this);
        toFrom = vector;
        to.definition().related().addRelationship(vector);
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
