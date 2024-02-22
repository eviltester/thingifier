package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

import java.util.*;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class EntityInstanceRelationships {

    private final List<RelationshipVectorInstance> relationships;
    private final EntityInstance forThis;

    public EntityInstanceRelationships(final EntityInstance thingInstance){
        this.forThis = thingInstance;
        this.relationships = new ArrayList<>();
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        if (!relationships.isEmpty()) {
            output.append(String.format("\t\t\t\t\t Relationships:%n"));
            for (RelationshipVectorInstance relatesTo : relationships) {
                output.append("\t\t\t\t\t" + relatesTo.toString());
            }
        }

        return output.toString();
    }

    public void connect(final String relationshipName, final EntityInstance thing) {
        // TODO: enforce cardinality

        final EntityDefinition entityDefinition = forThis.getEntity();

        // check if relationship is defined
        if (!entityDefinition.related().hasRelationship(relationshipName)) {
            throw new IllegalArgumentException(String.format("Unknown Relationship %s for %s : %s",
                    relationshipName, entityDefinition.getName(), forThis.getInternalId()));
        }

        // get the relationship vector between this thing and the passed in thing
        RelationshipVectorDefinition relationship = entityDefinition.
                                            getNamedRelationshipTo(
                                                    relationshipName,
                                                    thing.getEntity());

        if (relationship==null) {
            throw new IllegalArgumentException(
                    String.format("Unknown Relationship %s for %s : %s",
                        relationshipName, entityDefinition.getName(),
                        thing.getEntity().getName()));
        }


        RelationshipVectorInstance related = new RelationshipVectorInstance(
                                                relationship,
                                                forThis, thing);

        add(related);

        if (relationship.getRelationshipDefinition().isTwoWay()) {
            thing.getRelationships().add(related);
        }

    }

    private void add(final RelationshipVectorInstance relationship) {

        String instanceIdentification = "";

        try{
            instanceIdentification = forThis.getInternalId();
        }catch(Exception e){
            // ignore, no guid
        }

        // enforce validation
        if(!relationship.involves(forThis)){
            throw new RuntimeException(
                    String.format("Cannot add relationship to %s of type %s not valid",
                            instanceIdentification,
                            relationship.getDefinition().getName()));
        }

        if(relationship.getDefinition().getCardinality().hasMaximumLimit()){
            int maximumLimit = relationship.getDefinition().getCardinality().maximumLimit();
            if(relationships.size()>=maximumLimit){
                throw new RuntimeException(
                    String.format("Cannot add relationship type %s, exceeds maximum %d",
                            relationship.getRelationshipDefinition().getFromRelationship().getName(),
                            maximumLimit));
            }
        }

        relationships.add(relationship);
    }

    public EntityDefinition getTypeOfConnectableItems(final String relationshipName) {
        // This doesn't 'use' getConnectedItems because we might want to know the
        // types of related items, even if there are no actual relationships
        final EntityDefinition entityDefinition = forThis.getEntity();

        for (RelationshipVectorDefinition relationship : entityDefinition.related().getRelationships()) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                if (relationship.getTo() == entityDefinition) {
                    return relationship.getFrom();
                } else {
                    return relationship.getTo();
                }
            }
        }

        return null;
    }

    public Collection<EntityInstance> getConnectedItems(final String relationshipName) {
        Set<EntityInstance> theConnectedItems = new HashSet<>();
        for (RelationshipVectorInstance relationship : relationships) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                theConnectedItems.add(
                        relationship.getOtherThingInstance(forThis));
            }
        }

        return theConnectedItems;
    }

    public List<EntityInstance> getConnectedItemsOfType(final String type) {
        List<EntityInstance> theConnectedItems = new ArrayList<>();
        for (RelationshipVectorInstance relationship : relationships) {
            if (relationship.getTo().getEntity().getName().
                    toLowerCase().contentEquals(type.toLowerCase())) {
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }

    public List<EntityInstance> removeRelationshipsInvolving(final EntityInstance thing,
                                                             final String relationshipName) {

        List<EntityInstance> thingsToDelete = new ArrayList<>();
        List<RelationshipVectorInstance> toDelete = new ArrayList<>();

        for (RelationshipVectorInstance relationship : relationships) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                if (relationship.involves(thing)) {
                    toDelete.add(relationship);
                    thingsToDelete.addAll(relationship.instancesSubjectToMandatoryRelationship());
                    // delete any relationship to or from
                    thing.getRelationships().remove(relationship);
                }
            }
        }

        relationships.removeAll(toDelete);

        return thingsToDelete;
    }

    /*
        Remove all relationships and, as a knock on side-effect, return any of the
        'things' that are no longer valid since they were involved in a mandatory relationship.
     */
    public List<EntityInstance> removeAllRelationships() {
        List<EntityInstance> deleteThese = new ArrayList<>();

        final EntityInstance me = forThis;
        EntityInstance them;

        for (RelationshipVectorInstance relationship : relationships) {
            if (relationship.getFrom() == forThis) {
                // me -> them
                them= relationship.getTo();
            } else {
                // them -> me
                them = relationship.getFrom();
            }

            them.getRelationships().removeAllRelationshipsInvolving(me);
            deleteThese.addAll(relationship.instancesSubjectToMandatoryRelationship());
        }

        relationships.clear();

        return deleteThese;
    }

    private void remove(final RelationshipVectorInstance relationship) {
        relationships.remove(relationship);
    }

    public List<EntityInstance> removeAllRelationshipsInvolving(final EntityInstance thing) {

        List<EntityInstance> instancesToDelete = new ArrayList<>();

        List<RelationshipVectorInstance> toDelete = new ArrayList<>();

        for (RelationshipVectorInstance relationship : relationships) {
            if(relationship.involves(thing)){
                toDelete.add(relationship);
                instancesToDelete.addAll(relationship.instancesSubjectToMandatoryRelationship());
            }
        }

        relationships.removeAll(toDelete);

        return instancesToDelete;
    }

    public boolean hasAnyRelationshipInstances() {
        return !relationships.isEmpty();
    }

    public ValidationReport validateRelationships() {
        ValidationReport report = new ValidationReport();

        final EntityDefinition entityDefinition = forThis.getEntity();


        // Optionality Relationship Validation
        final Collection<RelationshipVectorDefinition> theRelationshipVectorDefns = entityDefinition.related().getRelationships();
        for(RelationshipVectorDefinition vector : theRelationshipVectorDefns){

            int foundRelationshipCount = 0;
            for(RelationshipVectorInstance relationship : relationships){
                if(relationship.getRelationshipDefinition()==vector.getRelationshipDefinition()){
                    foundRelationshipCount++;
                }
            }

            // for each definition vector, does it have relationships Vector Instances that match
            if(vector.getOptionality() == MANDATORY_RELATIONSHIP){
                if(foundRelationshipCount==0){
                    report.setValid(false).
                            addErrorMessage(String.format("Mandatory Relationship not found %s", vector.getName())
                    );
                }
            }

            // check cardinality here
            if(vector.getCardinality().hasMaximumLimit()){
                if(foundRelationshipCount>vector.getCardinality().maximumLimit()){
                    report.setValid(false).
                            addErrorMessage(String.format("Maximum related instances exceeded for %s at %d",
                                    vector.getName(), vector.getCardinality().maximumLimit())) ;
                }
            }
        }

        // validate each instance in detail
        for(RelationshipVectorInstance relationship : relationships){
            ValidationReport vectorInstanceReport = relationship.validate();
            if(!vectorInstanceReport.isValid()){
                Collection<String> errorMessages =vectorInstanceReport.getErrorMessages();
                for(String errorMessage : errorMessages){
                    report.setValid(false).addErrorMessage(
                            String.format("Error with EntityInstance relationship %s - %s",
                            forThis.getInternalId(), errorMessage)
                    );
                }
            }
        }


        return report;
    }
}
