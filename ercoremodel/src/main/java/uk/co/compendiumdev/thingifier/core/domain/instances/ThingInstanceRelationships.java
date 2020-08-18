package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

import java.util.*;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class ThingInstanceRelationships {

    private final List<RelationshipInstance> relationships;
    private final ThingInstance forThis;

    public ThingInstanceRelationships(final ThingInstance thingInstance){
        this.forThis = thingInstance;
        this.relationships = new ArrayList<>();
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        if (!relationships.isEmpty()) {
            output.append(String.format("\t\t\t\t\t Relationships:%n"));
            for (RelationshipInstance relatesTo : relationships) {
                output.append("\t\t\t\t\t" + relatesTo.toString());
            }
        }

        return output.toString();
    }

    public void connect(final String relationshipName, final ThingInstance thing) {
        // TODO: enforce cardinality

        final ThingDefinition entityDefinition = forThis.getEntity();

        // check if relationship is defined
        if (!entityDefinition.related().hasRelationship(relationshipName)) {
            throw new IllegalArgumentException(String.format("Unknown Relationship %s for %s : %s",
                    relationshipName, entityDefinition.getName(), forThis.getGUID()));
        }

        // get the relationship vector between this thing and the passed in thing
        RelationshipVector relationship = entityDefinition.
                                            getNamedRelationshipTo(
                                                    relationshipName,
                                                    thing.getEntity());

        if (relationship==null) {
            throw new IllegalArgumentException(
                    String.format("Unknown Relationship %s for %s : %s",
                        relationshipName, entityDefinition.getName(),
                        thing.getEntity().getName()));
        }


        RelationshipDefinition relationshipDefinition =
                        relationship.getRelationshipDefinition();

        RelationshipInstance related = new RelationshipInstance(
                                                relationshipDefinition,
                                                forThis, thing);
        this.relationships.add(related);

        if (relationshipDefinition.isTwoWay()) {
            thing.getRelationships().add(related);
        }

    }

    private void add(final RelationshipInstance relationship) {
        relationships.add(relationship);
    }

    public ThingDefinition getTypeOfConnectableItems(final String relationshipName) {
        // This doesn't 'use' getConnectedItems because we might want to know the
        // types of related items, even if there are no actual relationships
        final ThingDefinition entityDefinition = forThis.getEntity();

        for (RelationshipVector relationship : entityDefinition.related().getRelationships()) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                if (relationship.getTo().definition() == entityDefinition) {
                    return relationship.getFrom().definition();
                } else {
                    return relationship.getTo().definition();
                }
            }
        }

        return null;
    }

    public Collection<ThingInstance> getConnectedItems(final String relationshipName) {
        Set<ThingInstance> theConnectedItems = new HashSet<>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
                theConnectedItems.add(
                        relationship.getOtherThingInstance(forThis));
            }
        }

        return theConnectedItems;
    }

    public List<ThingInstance> getConnectedItemsOfType(final String type) {
        List<ThingInstance> theConnectedItems = new ArrayList<>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getTo().getEntity().getName().
                    toLowerCase().contentEquals(type.toLowerCase())) {
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }

    public List<ThingInstance> removeRelationshipsInvolving(final ThingInstance thing,
                                                            final String relationshipName) {

        List<ThingInstance> thingsToDelete = new ArrayList<>();
        List<RelationshipInstance> toDelete = new ArrayList<>();

        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
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
    public List<ThingInstance> removeAllRelationships() {
        List<ThingInstance> deleteThese = new ArrayList<>();

        final ThingInstance me = forThis;
        ThingInstance them;

        for (RelationshipInstance relationship : relationships) {
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

    private void remove(final RelationshipInstance relationship) {
        relationships.remove(relationship);
    }

    public List<ThingInstance> removeAllRelationshipsInvolving(final ThingInstance thing) {

        List<ThingInstance> deleteThings = new ArrayList<>();

        List<RelationshipInstance> toDelete = new ArrayList<>();

        for (RelationshipInstance relationship : relationships) {
            if(relationship.involves(thing)){
                toDelete.add(relationship);
                deleteThings.addAll(relationship.instancesSubjectToMandatoryRelationship());
            }
        }

        relationships.removeAll(toDelete);

        return deleteThings;
    }



    public boolean hasAnyRelationshipInstances() {
        return !relationships.isEmpty();
    }



    public ValidationReport validateRelationships() {
        ValidationReport report = new ValidationReport();


        // TODO: relationship cardinality validation e.g. too many, not enough etc.

        final ThingDefinition entityDefinition = forThis.getEntity();

        // Optionality Relationship Validation
        final Collection<RelationshipVector> theRelationshipVectors = entityDefinition.related().getRelationships();
        for(RelationshipVector vector : theRelationshipVectors){
            // for each definition, does it have relationships that match
            if(vector.getOptionality() == MANDATORY_RELATIONSHIP){
                boolean foundRelationship = false;
                for(RelationshipInstance relationship : relationships){
                    if(relationship.getRelationship()==vector.getRelationshipDefinition()){
                        foundRelationship=true;
                    }
                }
                if(!foundRelationship){
                    report.combine(
                            new ValidationReport().
                                    setValid(false).
                                    addErrorMessage(String.format("Mandatory Relationship not found %s", vector.getName()))
                    );
                }
            }
        }

        return report;
    }
}
