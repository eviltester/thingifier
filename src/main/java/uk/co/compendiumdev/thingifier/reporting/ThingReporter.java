package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.RelationshipInstance;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.List;
import java.util.Map;

public class ThingReporter {

    Map<String, Thing> things;
    private Map<String, RelationshipDefinition> relationships;

    public ThingReporter(Map<String, Thing> things,
                         Map<String, RelationshipDefinition>  relationships) {

        this.things = things;
        this.relationships = relationships;
    }

    public String basicReport() {
        StringBuilder output = new StringBuilder();

        output.append("\nThings:\n");
        output.append("=======\n");

        for(Thing aThing : things.values()){
            output.append(aThing.definition());
        }


        output.append("\nRelationships\n");
        output.append("=============\n");

        for(RelationshipDefinition aRelationship : relationships.values()){
            output.append(aRelationship);
        }

        output.append("\nInstances\n");
        output.append("=========\n");

        for(Thing aThing : things.values()){

            output.append("## Of " + aThing.definition().getName() + "\n");

            for(ThingInstance anInstance : aThing.getInstances()) {
                output.append(anInstance);
            }
        }


        return output.toString();
    }
}
