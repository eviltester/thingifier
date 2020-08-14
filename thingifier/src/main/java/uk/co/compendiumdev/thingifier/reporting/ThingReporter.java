package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Collection;


public class ThingReporter {

    private Thingifier thingifier;
    private Collection<Thing> things;
    private Collection<RelationshipDefinition> relationships;

    public ThingReporter(Thingifier thingifier) {
        this.things = thingifier.getThings();
        this.relationships = thingifier.getRelationshipDefinitions();
        this.thingifier = thingifier;
    }

    public String basicReport() {
        StringBuilder output = new StringBuilder();

        output.append("\nThings:\n");
        output.append("=======\n");

        for (Thing aThing : things) {
            output.append(aThing.definition());
        }


        output.append("\nRelationships\n");
        output.append("=============\n");

        for (RelationshipDefinition aRelationship : relationships) {
            output.append(aRelationship);
        }

        output.append("\nInstances\n");
        output.append("=========\n");

        for (Thing aThing : things) {

            output.append("## Of " + aThing.definition().getName() + "\n");

            for (ThingInstance anInstance : aThing.getInstances()) {
                output.append(anInstance);
            }
        }


        return output.toString();
    }


}
