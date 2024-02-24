package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;
import java.util.Map;

public class BodyRelationshipValidator {
    private final Thingifier thingifier;

    public BodyRelationshipValidator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ValidationReport validate(final BodyParser bodyargs, final EntityInstanceCollection thing, final String database) {
        final EntityDefinition thingDefinition = thing.definition();
        return validate(bodyargs, thingDefinition, database);
    }

    public ValidationReport validate(final BodyParser bodyargs, final EntityDefinition thingDefinition, final String database) {
        final ValidationReport report = new ValidationReport();

        List<Map.Entry<String,String>> fullargs = bodyargs.getFlattenedStringMap();

        boolean validRelationships = true;

        for(Map.Entry<String, String> complexKeyValue : fullargs){
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if(complexKey.startsWith("relationships.")){
                if(!validateComplexFourPartRelationshipDefinition(thingDefinition, report, complexKey, complexKeyValue.getValue(), database)){
                    validRelationships=false;
                }
            }else{
                if(complexKey.contains(".")){
                    // it might be a relationship
                    String[] parts = complexKey.split("\\.");
                    if(thingDefinition.related().hasRelationship(parts[0])) {
                        validRelationships = validateCompressedRelationshipDefinition(thingDefinition, report, complexKey, complexKeyValue.getValue(), database);
                    }
                }
            }
        }

        report.setValid(validRelationships);
        return report;
    }

    private boolean validateCompressedRelationshipDefinition(
            final EntityDefinition thingDefinition, final ValidationReport report,
            final String complexKey, final String complexKeyValue, final String database) {

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
        List<String> linkingFields = thingDefinition.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        if(!linkingFields.contains(fieldToMatchForGuid)){
            report.addErrorMessage(String.format(
                    "Do not support relationship references using %s", fieldToMatchForGuid));
            return false;
        }

        EntityInstance thingToRelateTo = thingifier.findThingInstanceByGuid(guidValue, database);
        // if we cannot find it by a guid then we need to identify the relationship type and find it by id for things
        if(thingToRelateTo==null) {
            final List<RelationshipVectorDefinition> relationshipsNamed = thingDefinition.related().getRelationships(relationShipName);
            for(RelationshipVectorDefinition vector : relationshipsNamed){
                final EntityInstanceCollection thingToRelationship = thingifier.getThingInstancesNamed(vector.getTo().getName(), database);
                thingToRelateTo = thingToRelationship.findInstanceByPrimaryKey(guidValue);
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
            final EntityDefinition thingDefinition, final ValidationReport report,
            final String complexKey, final String complexKeyValue, final String database) {
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
        EntityInstance thingToRelateTo = null;

        EntityInstanceCollection things = thingifier.
                getInstancesForSingularOrPluralNamedEntity(relationshipToPart, database);

        if(things!=null){
            thingToRelateTo = things.findInstanceByPrimaryKey(uniqueId);
        }

        // haven't found it yet
        if(thingToRelateTo==null){
            things = thingifier.getInstancesForSingularOrPluralNamedEntity(relationshipToPart, database);

            if(things!=null) {
                thingToRelateTo = things.findInstanceByFieldNameAndValue(relationshipFieldPart, uniqueId);
            }
        }

        if(thingToRelateTo==null){
            report.addErrorMessage(
                    String.format("cannot find %s of %s to relate to with %s %s",
                            relationshipFieldPart, relationshipToPart, relationshipFieldPart, uniqueId));
            return false;
        }

        return true;
    }


    private boolean isValidRelationship(final EntityDefinition thingDefinition,
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

    private boolean validRelationshipBetweenThings(final EntityDefinition thingDefinition,
                                                   final String relationShipName,
                                                   final String thingToRelateTo,
                                                   final ValidationReport report) {
        // is it a valid relationship to the other thing

        for(RelationshipVectorDefinition relationships : thingDefinition.related().getRelationships(relationShipName)){
            if(relationships.getTo().getPlural().equals(
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
