package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;

public class RelationshipDefinition {

    /*
        A RelationshipDefinition defines the whole scope of the relationship
        Things will only know about the specific Relationship vectors that relate outwards from themselves.
     */
    private RelationshipVector fromTo;
    private RelationshipVector toFrom;

    //todo: in theory we don't need 'relationship' since we could just have two vectors

    private RelationshipDefinition(RelationshipVector fromVector) {
        fromTo = fromVector;
        fromVector.forRelationship(this);
    }

    public static RelationshipDefinition create(RelationshipVector fromVector) {
        return new RelationshipDefinition(fromVector);
    }

    public RelationshipDefinition whenReversed(Cardinality of, String named) {
        final RelationshipVector vector = new RelationshipVector(
                fromTo.getTo(),
                named,
                fromTo.getFrom(),
                of);
        vector.forRelationship(this);
        toFrom = vector;
        return this;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();
        // TODO: the <=> should reflect the cardinality defined vectors created
        if (toFrom == null) {
            output.append(String.format("\t\t%s : %s <=> %s %n",
                    fromTo.getName(),
                    fromTo.getFrom().definition().getName(),
                    fromTo.getTo().definition().getName()));
        } else {
            output.append(String.format("\t\t%1$s : %2$s <%1$s/%4$s> %3$s %n",
                    fromTo.getName(),
                    fromTo.getFrom().definition().getName(),
                    fromTo.getTo().definition().getName(),
                    toFrom.getName()));

        }

        return output.toString();
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
}
