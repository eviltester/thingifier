package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class RelationshipVectorInstance {

    private RelationshipVectorDefinition relationshipVector;
    private EntityInstance from;
    private EntityInstance to;

    // a vector instance because it represents a from / to
    public RelationshipVectorInstance(RelationshipVectorDefinition relationshipVector, EntityInstance from, EntityInstance to) {
        this.from = from;
        this.to = to;
        this.relationshipVector = relationshipVector;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        String format = String.format("%s FROM: %s %s TO: %s %s",
                relationshipVector.getName(),
                from.getEntity().getName(),
                from.getInternalId(),
                to.getEntity().getName(),
                to.getInternalId()
        );

        output.append(format + "\n");

        return output.toString();
    }

    public RelationshipDefinition getRelationshipDefinition() {
        return relationshipVector.getRelationshipDefinition();
    }

    public RelationshipVectorDefinition getDefinition() {
        return relationshipVector;
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

                final RelationshipVectorDefinition otherVector = relationshipVector.getRelationshipDefinition().
                                                        otherVectorOf(relationshipVector);

                if(otherVector.getOptionality() == MANDATORY_RELATIONSHIP) {
                    // if relationship deleted therefore the other thing should be deleted too
                    // since the relationship to other is mandatory
                    deleteThese.add(to);
                }
        }

        return deleteThese;
    }

    public ValidationReport validate() {

        final ValidationReport report = new ValidationReport();

        // valid when
        // we have both sides on the vector
        // we have a relationship definition
        // the items on either side match the relationship definition

        if(relationshipVector==null){
            report.setValid(false).
                    addErrorMessage("No Relationship found");
        }

        if(from==null){
            report.setValid(false).
                    addErrorMessage("No From Instance found");
        }

        if(to==null){
            report.setValid(false).
                    addErrorMessage("No To Instance found");
        }

        // short cut validation checking if something major wrong
        if(!report.isValid()){
            return report;
        }

        if(from.getEntity() != relationshipVector.getFrom()){
            report.setValid(false).
                    addErrorMessage(
                        String.format("Found from EntityInstance types %s but expected of type %s",
                                from.getEntity().getName(), relationshipVector.getFrom().getName()));
        }

        if(to.getEntity() != relationshipVector.getTo()){
            report.setValid(false).
                    addErrorMessage(
                            String.format("Found to EntityInstance types %s but expected of type %s",
                                    to.getEntity().getName(), relationshipVector.getTo().getName()));
        }

        return report;
    }


}
