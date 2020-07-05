package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BodyRelationshipValidator {
    private final Thingifier thingifier;

    public BodyRelationshipValidator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ValidationReport validate(final BodyParser bodyargs, final Thing thing) {
        final ValidationReport report = new ValidationReport();

        Map<String,String> fullargs = bodyargs.getFlattenedStringMap();

        // todo: check that all relationship definitions are valid
        boolean validRelationships = true;

        for(Map.Entry<String, String> complexKeyValue : fullargs.entrySet()){
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if(complexKey.startsWith("relationships.")){
                String[] parts = complexKey.split("\\.");
                if(parts.length!=4){
                    // invalid relationship
                    validRelationships = false;
                    report.addErrorMessage(String.format("%s is not a valid relationship",complexKey));
                    continue;
                }
                // is it a valid relationship name for this thing
                if(!thing.definition().hasRelationship(parts[1])){
                    validRelationships = false;
                    report.addErrorMessage(
                            String.format("%s is not a valid relationship for %s",
                                    parts[1], thing.definition().getName()));
                    continue;
                }
                // is it a valid relationship to the other thing
                boolean foundRelationship=false;
                for(RelationshipVector relationships : thing.definition().getRelationships(parts[1])){
                    if(relationships.getTo().definition().getPlural().equals(parts[2])){
                        foundRelationship=true;
                    }
                }
                if(!foundRelationship){
                    validRelationships=false;
                    report.addErrorMessage(
                            String.format("%s to %s is not a valid relationship for %s",
                                    parts[1], parts[2], thing.definition().getName()));
                    continue;
                }
                // check that the thing we want to relate with exists
                String guid = complexKeyValue.getValue();
                final ThingInstance thingToRelateTo = thingifier.
                        getThingNamedSingularOrPlural(parts[2]).findInstanceByGUID(guid);
                if(thingToRelateTo==null){
                    validRelationships=false;
                    report.addErrorMessage(
                            String.format("cannot find %s of %s to relate to with guid %s",
                                    parts[3], parts[2], guid));
                }
            }
        }
        report.setValid(validRelationships);
        return report;
    }
}
