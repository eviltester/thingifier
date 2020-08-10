package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

import java.util.*;

import static uk.co.compendiumdev.thingifier.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

public class Relationships {

    private final List<RelationshipInstance> relationships;
    private final ThingInstance forThis;
    private final ThingDefinition entityDefinition;

    public Relationships(final ThingInstance thingInstance){
        this.forThis = thingInstance;
        this.entityDefinition = thingInstance.getEntity();
        this.relationships = new ArrayList<>();
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        if (relationships.size() > 0) {
            output.append(String.format("\t\t\t\t\t Relationships:%n"));
            for (RelationshipInstance relatesTo : relationships) {
                if (relatesTo.getFrom() == forThis) {
                    output.append(String.format("\t\t\t\t\t %s : %s (%s)%n",
                            relatesTo.getRelationship().getName(),
                            relatesTo.getTo().getGUID(),
                            relatesTo.getTo().getEntity().getName()));
                } else {
                    output.append(String.format("\t\t\t\t\t %s : %s (%s)%n",
                            relatesTo.getRelationship().getReversedRelationship().getName(),
                            relatesTo.getFrom().getGUID(),
                            relatesTo.getFrom().getEntity().getName()));
                }
            }
        }

        return output.toString();
    }

    public void connect(final String relationshipName, final ThingInstance thing) {
        // TODO: enforce cardinality

        // find relationship
        if (!entityDefinition.related().hasRelationship(relationshipName)) {
            throw new IllegalArgumentException(String.format("Unknown Relationship %s for %s : %s",
                    relationshipName, entityDefinition.getName(), forThis.getGUID()));
        }

        RelationshipVector relationship = entityDefinition.related().getRelationship(relationshipName, thing.getEntity());

        RelationshipInstance related = new RelationshipInstance(
                                                relationship.getRelationshipDefinition(),
                                                forThis, thing);
        this.relationships.add(related);

        thing.isNowRelatedVia(related);
    }

    public void add(final RelationshipInstance relationship) {
        relationships.add(relationship);
    }

    public ThingDefinition getTypeOfConnectedItems(final String relationshipName) {

        for (RelationshipVector relationship : entityDefinition.related().getRelationships()) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                if (relationship.getTo().definition() == this.entityDefinition) {
                    return relationship.getFrom().definition();
                } else {
                    return relationship.getTo().definition();
                }
            }
        }

        return null;
    }

    public Collection<ThingInstance> getConnectedItems(final String relationshipName) {
        Set<ThingInstance> theConnectedItems = new HashSet<ThingInstance>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
                if (relationship.getTo() == forThis) {
                    theConnectedItems.add(relationship.getFrom());
                } else {
                    theConnectedItems.add(relationship.getTo());
                }
            }
        }

        return theConnectedItems;
    }

    public void removeRelationshipsTo(final ThingInstance thing, final String relationshipName) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
                if (relationship.getTo() == thing || relationship.getFrom() == thing) {
                    toDelete.add(relationship);
                    thing.isNoLongerRelatedVia(relationship);
                }
            }
        }

        relationships.removeAll(toDelete);
    }

    public List<ThingInstance> removeAllRelationships() {
        List<ThingInstance> deleteThese = new ArrayList<>();

        for (RelationshipInstance item : relationships) {
            if (item.getFrom() != forThis) {
                item.getFrom().removeRelationshipsInvolvingMe(forThis);
                if (item.getRelationship().getOptionalityFrom() == MANDATORY_RELATIONSHIP) {
                    // I am deleted, therefor any mandatory relationship to me, must result in the related thing being
                    // deleted also
                    deleteThese.add(item.getFrom());
                }
            } else {
                item.getTo().removeRelationshipsInvolvingMe(forThis);

//                if (item.getRelationship().getOptionalityTo() == MANDATORY_RELATIONSHIP) {
//                    // I am being deleted therefore it does not matter if relationship to other is mandatory
//                }
            }
        }

        relationships.clear();

        return deleteThese;
    }

    public void remove(final RelationshipInstance relationship) {
        relationships.remove(relationship);
    }

    public void removeAllRelationshipsInvolving(final ThingInstance thing) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for (RelationshipInstance relationship : relationships) {
            if (relationship.getTo() == thing) {
                toDelete.add(relationship);
            }
            if (relationship.getFrom() == thing) {
                toDelete.add(relationship);
            }
        }

        relationships.removeAll(toDelete);
    }

    public List<ThingInstance> connectedItemsOfType(final String type) {
        List<ThingInstance> theConnectedItems = new ArrayList<ThingInstance>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getTo().getEntity().getName().toLowerCase().contentEquals(type.toLowerCase())) {
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }

    public boolean hasAnyRelationshipInstances() {
        return (relationships.size() >0);
    }

    public List<RelationshipInstance> createClonedRelationships() {
        List<RelationshipInstance> cloned = new ArrayList<>();
        for(RelationshipInstance relationship : relationships){
            cloned.add( new RelationshipInstance(
                                relationship.getRelationship(),
                                relationship.getFrom(),
                                relationship.getTo()
                        )
                    );
        }
        return cloned;
    }

    public ValidationReport validateRelationships() {
        ValidationReport report = new ValidationReport();


        // TODO: relationship cardinality validation e.g. too many, not enough etc.

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
