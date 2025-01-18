package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public class FieldAutoIncrementValidationRule implements ValidationRule{

    public boolean validates(FieldValue value){
        // if this rule is present then it is because it is an auto increment field
        return false;
    }

    public String getErrorMessage(FieldValue value){
        return String.format(
                "%s : field is an ID, you can't set it", value.getName());
    }

    public String getExplanation(){
        return "Field is an ID and should not be amended or set";
    }
}
