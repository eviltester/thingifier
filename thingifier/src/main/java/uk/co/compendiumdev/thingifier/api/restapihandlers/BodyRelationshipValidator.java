package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
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
        final ThingDefinition thingDefinition = thing.definition();
        return validate(bodyargs, thingDefinition);
    }

    public ValidationReport validate(final BodyParser bodyargs, final ThingDefinition thingDefinition) {
        final ValidationReport report = new ValidationReport();

        List<Map.Entry<String,String>> fullargs = bodyargs.getFlattenedStringMap();

        boolean validRelationships = true;

        for(Map.Entry<String, String> complexKeyValue : fullargs){
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if(complexKey.startsWith("relationships.")){
                if(!validateComplexFourPartRelationshipDefinition(thingDefinition, report, complexKey, complexKeyValue.getValue())){
                    validRelationships=false;
                };
            }else{
                if(complexKey.contains(".")){
                    // it might be a relationship
                    String[] parts = complexKey.split("\\.");
                    if(thingDefinition.hasRelationship(parts[0])) {
                        validRelationships = validateCompressedRelationshipDefinition(thingDefinition, report, complexKey, complexKeyValue.getValue());
                    }
                }
            }
        }

        report.setValid(validRelationships);
        return report;
    }

    private boolean validateCompressedRelationshipDefinition(
            final ThingDefinition thingDefinition, final ValidationReport report,
            final String complexKey, final String complexKeyValue) {

        boolean validRelationships=true;
        String[] parts = complexKey.split("\\.");

        if(parts.length!=2){
            // invalid relationship
            validRelationships = false;
            report.addErrorMessage(String.format("%s is not a valid relationship",complexKey));
            return validRelationships;
        }
        String relationShipName = parts[0];
        String fieldToMatchForGuid = parts[1];
        String guidValue = complexKeyValue;

        // is it a valid relationship name for this thing
        if(!thingDefinition.hasRelationship(relationShipName)){
            validRelationships = false;
            report.addErrorMessage(
                    String.format("%s is not a valid relationship for %s",
                            relationShipName, thingDefinition.getName()));
            return validRelationships;
        }

        // find the other thing
        if(!fieldToMatchForGuid.equals("guid")){
            validRelationships = false;
            report.addErrorMessage(String.format(
                    "Only support relationship references for guid, not for %s", fieldToMatchForGuid));
            return validRelationships;
        }

        final ThingInstance thingToRelateTo = thingifier.findThingInstanceByGuid(guidValue);
        if(thingToRelateTo==null){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("cannot find %s to relate to with guid %s",
                            relationShipName, guidValue));
            return validRelationships;
        }

        // is it a valid relationship to the other thing
        boolean foundRelationship=false;
        for(RelationshipVector relationships : thingDefinition.getRelationships(relationShipName)){
            if(relationships.getTo().definition().getPlural().equals(
                            thingToRelateTo.getEntity().getPlural())){
                foundRelationship=true;
            }
        }
        if(!foundRelationship){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("%s to %s is not a valid relationship for %s",
                            relationShipName, thingToRelateTo.getEntity().getName(), thingDefinition.getName()));
            return validRelationships;
        }
        // check that the thing we want to relate with exists
        return validRelationships;
    }

    private boolean validateComplexFourPartRelationshipDefinition(
                    final ThingDefinition thingDefinition, final ValidationReport report,
                    final String complexKey, final String complexKeyValue) {
        String[] parts = complexKey.split("\\.");
        Boolean validRelationships = true;
        if(parts.length!=4){
            // invalid relationship
            validRelationships = false;
            report.addErrorMessage(String.format("%s is not a valid relationship",complexKey));
            return validRelationships;
        }
        // is it a valid relationship name for this thing
        if(!thingDefinition.hasRelationship(parts[1])){
            validRelationships = false;
            report.addErrorMessage(
                    String.format("%s is not a valid relationship for %s",
                            parts[1], thingDefinition.getName()));
            return validRelationships;
        }
        // is it a valid relationship to the other thing
        boolean foundRelationship=false;
        for(RelationshipVector relationships : thingDefinition.getRelationships(parts[1])){
            if(relationships.getTo().definition().getPlural().equals(parts[2])){
                foundRelationship=true;
            }
        }
        if(!foundRelationship){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("%s to %s is not a valid relationship for %s",
                            parts[1], parts[2], thingDefinition.getName()));
            return validRelationships;
        }
        // check that the thing we want to relate with exists
        String guid = complexKeyValue;
        final ThingInstance thingToRelateTo = thingifier.
                getThingNamedSingularOrPlural(parts[2]).findInstanceByGUID(guid);
        if(thingToRelateTo==null){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("cannot find %s of %s to relate to with guid %s",
                            parts[3], parts[2], guid));
        }
        return validRelationships;
    }
}
