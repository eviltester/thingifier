package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class RelationshipVectorInstance {

    private RelationshipVector relationshipVector;
    private EntityInstance from;
    private EntityInstance to;

    // a vector instance because it represents a from / to
    public RelationshipVectorInstance(RelationshipVector relationshipVector, EntityInstance from, EntityInstance to) {
        this.from = from;
        this.to = to;
        this.relationshipVector = relationshipVector;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        String format = String.format("%s FROM: %s %s TO: %s %s",
                relationshipVector.getName(),
                from.getEntity().getName(),
                from.getGUID(),
                to.getEntity().getName(),
                to.getGUID()
        );

        output.append(format + "\n");

        return output.toString();
    }

    public RelationshipDefinition getRelationshipDefinition() {
        return relationshipVector.getRelationshipDefinition();
    }

    public EntityInstance getTo() {
        return to;
    }

    public EntityInstance getFrom() {
        return from;
    }

    public boolean involves(final EntityInstance thing) {
        return (to == thing || from == thing);
    }

    public EntityInstance getOtherThingInstance(final EntityInstance forThis) {
        if (to == forThis) {
            return from;
        }

        return to;
    }

    public List<EntityInstance> instancesSubjectToMandatoryRelationship() {

        List<EntityInstance> deleteThese = new ArrayList<>();

        if (relationshipVector.getOptionality() == MANDATORY_RELATIONSHIP) {
            // mandatory relationship must result in the from thing being deleted
            deleteThese.add(from);
        }

        if (relationshipVector.getRelationshipDefinition().isTwoWay()){

                final RelationshipVector otherVector = relationshipVector.getRelationshipDefinition().
                                                        otherVectorOf(relationshipVector);

                if(otherVector.getOptionality() == MANDATORY_RELATIONSHIP) {
                    // if relationship deleted therefore the other thing should be deleted too
                    // since the relationship to other is mandatory
                    deleteThese.add(to);
                }
        }

        return deleteThese;
    }
}
