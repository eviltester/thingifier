package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class RelationshipInstance {

    private RelationshipDefinition relationship;
    private ThingInstance from;
    private ThingInstance to;

    // todo: this should be a vector not a RelationshipDefinition because it represents a from / to
    // todo: rename to RelationshipVectorInstance
    public RelationshipInstance(RelationshipDefinition relationship, ThingInstance from, ThingInstance to) {
        this.from = from;
        this.to = to;
        this.relationship = relationship;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        String format = String.format("%s FROM: %s %s TO: %s %s",
                relationship.getFromRelationship().getName(),
                from.getEntity().getName(),
                from.getGUID(),
                to.getEntity().getName(),
                to.getGUID()
        );

        output.append(format + "\n");

        return output.toString();
    }

    public RelationshipDefinition getRelationship() {
        return relationship;
    }

    public ThingInstance getTo() {
        return to;
    }

    public ThingInstance getFrom() {
        return from;
    }

    public boolean involves(final ThingInstance thing) {
        return (to == thing || from == thing);
    }

    public ThingInstance getOtherThingInstance(final ThingInstance forThis) {
        if (to == forThis) {
            return from;
        }

        return to;
    }

    public List<ThingInstance> instancesSubjectToMandatoryRelationship() {

        List<ThingInstance> deleteThese = new ArrayList<>();

        final RelationshipVector fromVector = relationship.getFromRelationship();

        if (fromVector.getOptionality() == MANDATORY_RELATIONSHIP) {
            // mandatory relationship must result in the from thing being deleted
            deleteThese.add(from);
        }

        final RelationshipVector twoWay = relationship.getReversedRelationship();
        if (twoWay!=null && twoWay.getOptionality() == MANDATORY_RELATIONSHIP) {
            // if relationship deleted therefore the other thing should be deleted too
            // since the relationship to other is mandatory
            deleteThese.add(to);
        }

        return deleteThese;
    }
}
