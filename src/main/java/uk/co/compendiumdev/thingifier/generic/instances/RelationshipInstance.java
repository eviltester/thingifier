package uk.co.compendiumdev.thingifier.generic.instances;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;

public class RelationshipInstance {
    private RelationshipDefinition relationship;
    private ThingInstance from;
    private ThingInstance to;
    private Thing representedBy;

    public RelationshipInstance(RelationshipVector relationship, ThingInstance from, ThingInstance to) {
        this.from = from;
        this.to = to;
        this.relationship = relationship.getRelationshipDefinition();
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

    public RelationshipInstance setRepresentedBy(Thing representedBy) {
        this.representedBy = representedBy;
        return this;
    }

    public String toString(){

        StringBuilder output = new StringBuilder();

        String format = String.format("%s FROM: %s %s TO: %s %s",
                relationship.getName(),
                from.getEntity().getName(),
                from.getGUID(),
                to.getEntity().getName(),
                to.getGUID()
                );

        output.append(format + "\n");

        // now output the instances
        for(ThingInstance instance : representedBy.getInstances()){
            output.append(instance + "\n");
        }

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
