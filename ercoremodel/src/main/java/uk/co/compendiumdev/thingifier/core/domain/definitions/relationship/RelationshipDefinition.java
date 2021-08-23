package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;

public class RelationshipDefinition {

    /*
        A RelationshipDefinition defines the whole scope of the relationship
        Things will only know about the specific Relationship vectors that relate outwards from themselves.
     */
    private RelationshipVectorDefinition fromTo;
    private RelationshipVectorDefinition toFrom;

    //todo: in theory we don't need 'relationship' since we could just have two vectors

    private RelationshipDefinition(RelationshipVectorDefinition fromVector) {
        fromTo = fromVector;
        fromVector.forRelationship(this);
    }

    public static RelationshipDefinition create(RelationshipVectorDefinition fromVector) {
        return new RelationshipDefinition(fromVector);
    }

    public RelationshipDefinition whenReversed(Cardinality of, String named) {
        final RelationshipVectorDefinition vector = new RelationshipVectorDefinition(
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
                    fromTo.getFrom().getName(),
                    fromTo.getTo().getName()));
        } else {
            output.append(String.format("\t\t%1$s : %2$s <%1$s/%4$s> %3$s %n",
                    fromTo.getName(),
                    fromTo.getFrom().getName(),
                    fromTo.getTo().getName(),
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
    public RelationshipVectorDefinition getReversedRelationship() {
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

    public RelationshipVectorDefinition getFromRelationship() {
        return fromTo;
    }

    /* given a vector in this relationship, return the other one */
    public RelationshipVectorDefinition otherVectorOf(final RelationshipVectorDefinition relationshipVector) {
        if(fromTo==relationshipVector){
            return toFrom;
        }
        if(toFrom==relationshipVector){
            return fromTo;
        }
        // this vector is not part of this relationship
        System.out.println(String.format("Vector %s not part of relationship",
                                relationshipVector.getName()));
        return null;
    }
}
