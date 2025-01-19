package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.math.BigDecimal;

public class IntegerValidationRule implements ValidationRule{

    private final Integer minimumIntegerValue;
    private final Integer maximumIntegerValue;

    public IntegerValidationRule(Integer minimumIntegerValue, Integer maximumIntegerValue){
        this.minimumIntegerValue = minimumIntegerValue;
        this.maximumIntegerValue = maximumIntegerValue;
    }

    private boolean validatesAgainstType(FieldValue value){
        try {
            int intVal = value.asInteger();

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean validates(FieldValue value){
            return validatesAgainstType(value) && withinAllowedIntegerRange(value.asInteger());
    }

    private boolean withinAllowedIntegerRange(final int intVal) {
        return (intVal>=minimumIntegerValue &&
                intVal<=maximumIntegerValue);
    }

    public String getErrorMessage(FieldValue value){
        if(!validatesAgainstType(value)){
            return TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(value, FieldType.INTEGER);
        }
        return String.format(
                "%s : %s is not within range for type %s (%d to %d)",
                value.getName(),
                value.asString(),
                FieldType.INTEGER, minimumIntegerValue, maximumIntegerValue);
    }

    public String getExplanation(){
        return String.format("Value must be an Integer of min %d and max %d", minimumIntegerValue, maximumIntegerValue);
    }
}
