package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipVector;

public class RelationshipInstance {

    private RelationshipDefinition relationship;
    private ThingInstance from;
    private ThingInstance to;

    // TODO : should a relationship instance be associated with a vector rather than a definition?
    public RelationshipInstance(RelationshipVector relationship, ThingInstance from, ThingInstance to) {
        this.from = from;
        this.to = to;
        this.relationship = relationship.getRelationshipDefinition();
    }

    public RelationshipInstance(RelationshipDefinition relationship, ThingInstance from, ThingInstance to) {
        this.from = from;
        this.to = to;
        this.relationship = relationship;
    }

    public RelationshipInstance setRelationship(RelationshipDefinition relationship) {
        this.relationship = relationship;
        return this;
    }

    public RelationshipInstance from(ThingInstance aThingInstance) {
        this.from = aThingInstance;
        return this;
    }

    public RelationshipInstance to(ThingInstance aThingInstance) {
        this.to = aThingInstance;
        return this;
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
        return relationship;
    }

    public ThingInstance getTo() {
        return to;
    }

    public ThingInstance getFrom() {
        return from;
    }

}
