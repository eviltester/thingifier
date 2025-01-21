package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public class MandatoryValidationRule implements ValidationRule{

    public boolean validates(FieldValue value){
        return value != null;
    }

    public String getErrorMessage(FieldValue value){
        return String.format(
                "%s : field is mandatory", value.getName());
    }

    public String getExplanation(){
        return "Value is mandatory";
    }
}
