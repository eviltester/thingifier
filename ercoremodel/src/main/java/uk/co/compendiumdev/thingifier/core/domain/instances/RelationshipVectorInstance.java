package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class RelationshipVectorInstance {

    private RelationshipVector relationship;
    private EntityInstance from;
    private EntityInstance to;

    // todo: this should be a vector not a RelationshipDefinition because it represents a from / to
    // todo: rename to RelationshipVectorInstance
    public RelationshipVectorInstance(RelationshipVector relationship, EntityInstance from, EntityInstance to) {
        this.from = from;
        this.to = to;
        this.relationship = relationship;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        String format = String.format("%s FROM: %s %s TO: %s %s",
                relationship.getName(),
                from.getEntity().getName(),
                from.getGUID(),
                to.getEntity().getName(),
                to.getGUID()
        );

        output.append(format + "\n");

        return output.toString();
    }

    public RelationshipDefinition getRelationship() {
        return relationship.getRelationshipDefinition();
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

        if (relationship.getOptionality() == MANDATORY_RELATIONSHIP) {
            // mandatory relationship must result in the from thing being deleted
            deleteThese.add(from);
        }

        if (relationship.getRelationshipDefinition().isTwoWay()){

                final RelationshipVector otherVector = relationship.getRelationshipDefinition().
                                                        otherVectorOf(relationship);

                if(otherVector.getOptionality() == MANDATORY_RELATIONSHIP) {
                    // if relationship deleted therefore the other thing should be deleted too
                    // since the relationship to other is mandatory
                    deleteThese.add(to);
                }
        }

        return deleteThese;
    }
}
