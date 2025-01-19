package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BooleanValidationRule implements ValidationRule{

    public boolean validates(FieldValue value){
        try{
            value.asBoolean();
            return true;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    public String getErrorMessage(FieldValue value){
        return String.format(
                "%s : %s does not match type %s (true, false)",
                value.getName(),
                value.asString(), FieldType.BOOLEAN);
    }

    public String getExplanation(){
        return "Value must be a Boolean (true, false) value";
    }

}
