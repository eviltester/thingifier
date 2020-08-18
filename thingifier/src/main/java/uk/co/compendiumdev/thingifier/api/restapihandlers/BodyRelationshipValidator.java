package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

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
                    if(thingDefinition.related().hasRelationship(parts[0])) {
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

        String[] parts = complexKey.split("\\.");

        if(parts.length!=2){
            reportIsNotValidRelationship(complexKey, report);
            return false;
        }

        String relationShipName = parts[0];
        String fieldToMatchForGuid = parts[1];
        String guidValue = complexKeyValue;

        // is it a valid relationship name for this thing
        if(!isValidRelationship(thingDefinition, relationShipName, report)){
            return false;
        }

        // can we relate via that field?
        List<String> linkingFields = thingDefinition.getFieldNamesOfType(FieldType.ID, FieldType.GUID);
        if(!linkingFields.contains(fieldToMatchForGuid)){
            report.addErrorMessage(String.format(
                    "Do not support relationship references using %s", fieldToMatchForGuid));
            return false;
        }

        ThingInstance thingToRelateTo = thingifier.findThingInstanceByGuid(guidValue);
        // if we cannot find it by a guid then we need to identify the relationship type and find it by id for things
        if(thingToRelateTo==null) {
            final List<RelationshipVector> relationshipsNamed = thingDefinition.related().getRelationships(relationShipName);
            for(RelationshipVector vector : relationshipsNamed){
                final Thing thingToRelationship = vector.getTo();
                thingToRelateTo = thingToRelationship.findInstanceByGUIDorID(guidValue);
                if(thingToRelateTo!=null){
                    break;
                }
            }
        }

        if(thingToRelateTo==null){
            report.addErrorMessage(
                    String.format("cannot find %s to relate to with %s %s",
                            relationShipName, fieldToMatchForGuid, guidValue));
            return false;
        }

        if(!validRelationshipBetweenThings(
                            thingDefinition, relationShipName,
                            thingToRelateTo.getEntity().getPlural()
                            , report)){
            return false;
        }

        // check that the thing we want to relate with exists
        return true;
    }



    private boolean validateComplexFourPartRelationshipDefinition(
                    final ThingDefinition thingDefinition, final ValidationReport report,
                    final String complexKey, final String complexKeyValue) {
        String[] parts = complexKey.split("\\.");

        if(parts.length!=4){
            reportIsNotValidRelationship(complexKey, report);
            return false;
        }

        String relationshipsPart = parts[0];
        String relationshipNamePart = parts[1];
        String relationshipToPart = parts[2];
        String relationshipFieldPart = parts[3];

        // is it a valid relationship name for this thing
        if(!isValidRelationship(thingDefinition, relationshipNamePart, report)){
            return false;
        }

        if(!validRelationshipBetweenThings(thingDefinition, relationshipNamePart,
                                relationshipToPart, report)){
            return false;
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
            report.addErrorMessage(
                    String.format("cannot find %s of %s to relate to with %s %s",
                            relationshipFieldPart, relationshipToPart, relationshipFieldPart, uniqueId));
            return false;
        }

        return true;
    }


    private boolean isValidRelationship(final ThingDefinition thingDefinition,
                                        final String relationShipName,
                                        final ValidationReport report) {
        if(!thingDefinition.related().hasRelationship(relationShipName)){
            report.addErrorMessage(
                    String.format("%s is not a valid relationship for %s",
                            relationShipName, thingDefinition.getName()));
            return false;
        }
        return true;
    }

    private void reportIsNotValidRelationship(final String relationshipToMention, ValidationReport report) {
        report.addErrorMessage(String.format("%s is not a valid relationship",relationshipToMention));
    }

    private boolean validRelationshipBetweenThings(final ThingDefinition thingDefinition,
                                                   final String relationShipName,
                                                   final String thingToRelateTo,
                                                   final ValidationReport report) {
        // is it a valid relationship to the other thing

        for(RelationshipVector relationships : thingDefinition.related().getRelationships(relationShipName)){
            if(relationships.getTo().definition().getPlural().equals(
                    thingToRelateTo)){
                return true;
            }
        }

        report.addErrorMessage(
                String.format("%s to %s is not a valid relationship for %s",
                        relationShipName, thingToRelateTo, thingDefinition.getName()));

        return false;
    }


}
