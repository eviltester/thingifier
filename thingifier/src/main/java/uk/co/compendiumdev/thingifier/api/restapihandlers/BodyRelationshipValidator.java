package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

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
        List<String> linkingFields = thingDefinition.getProtectedFieldNamesList();
        if(!linkingFields.contains(fieldToMatchForGuid)){
            validRelationships = false;
            report.addErrorMessage(String.format(
                    "Do not support relationship references using %s", fieldToMatchForGuid));
            return validRelationships;
        }

        ThingInstance thingToRelateTo = thingifier.findThingInstanceByGuid(guidValue);
        // if we cannot find it by a guid then we need to identify the relationship type and find it by id for things
        if(thingToRelateTo==null) {
            final List<RelationshipVector> relationshipsNamed = thingDefinition.getRelationships(relationShipName);
            for(RelationshipVector vector : relationshipsNamed){
                final Thing thingToRelationship = vector.getTo();
                thingToRelateTo = thingToRelationship.findInstanceByGUIDorID(guidValue);
                if(thingToRelateTo!=null){
                    break;
                }
            }
        }

        if(thingToRelateTo==null){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("cannot find %s to relate to with %s %s",
                            relationShipName, fieldToMatchForGuid, guidValue));
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

        String relationshipsPart = parts[0];
        String relationshipNamePart = parts[1];
        String relationshipToPart = parts[2];
        String relationshipFieldPart = parts[3];

        // is it a valid relationship name for this thing
        if(!thingDefinition.hasRelationship(relationshipNamePart)){
            validRelationships = false;
            report.addErrorMessage(
                    String.format("%s is not a valid relationship for %s",
                            relationshipNamePart, thingDefinition.getName()));
            return validRelationships;
        }
        // is it a valid relationship to the other thing
        boolean foundRelationship=false;
        for(RelationshipVector relationships : thingDefinition.getRelationships(relationshipNamePart)){
            if(relationships.getTo().definition().getPlural().equals(relationshipToPart)){
                foundRelationship=true;
            }
        }
        if(!foundRelationship){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("%s to %s is not a valid relationship for %s",
                            relationshipNamePart, relationshipToPart, thingDefinition.getName()));
            return validRelationships;
        }
        // check that the thing we want to relate with exists
        String uniqueId = complexKeyValue;
        ThingInstance thingToRelateTo = thingifier.
                getThingNamedSingularOrPlural(relationshipToPart).findInstanceByGUID(uniqueId);
        if(thingToRelateTo==null){
            thingToRelateTo = thingifier.
                    getThingNamedSingularOrPlural(relationshipToPart).
                        findInstanceByField(
                            FieldValue.is(relationshipFieldPart, uniqueId));
        }
        if(thingToRelateTo==null){
            validRelationships=false;
            report.addErrorMessage(
                    String.format("cannot find %s of %s to relate to with %s %s",
                            relationshipFieldPart, relationshipToPart, relationshipFieldPart, uniqueId));
        }
        return validRelationships;
    }
}
