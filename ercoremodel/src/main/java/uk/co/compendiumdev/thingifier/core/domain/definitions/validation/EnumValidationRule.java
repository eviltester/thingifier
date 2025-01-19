package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.List;

public class EnumValidationRule implements ValidationRule{

    private final List<String> validValues;

    public EnumValidationRule(List<String> enumValues){
        this.validValues = enumValues;
    }
    public boolean validates(FieldValue value){
        return validValues.contains(value.asString());
    }

    private String valuesAsCsv(){

        String prepend = "";
        StringBuilder csv = new StringBuilder();
        for(String value : validValues){
            csv.append(prepend);
            csv.append(value);
            prepend = ", ";
        }
        return csv.toString();
    }

    public String getErrorMessage(FieldValue value){
        return TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(value, FieldType.ENUM, validValues);
    }

    public String getExplanation(){
        return String.format("Value must be an Enum (%s) value", valuesAsCsv());
    }
}
