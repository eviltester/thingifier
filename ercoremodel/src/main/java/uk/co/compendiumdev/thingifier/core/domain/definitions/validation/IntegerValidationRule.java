package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.math.BigDecimal;

public class IntegerValidationRule implements ValidationRule{

    private final Integer minimumIntegerValue;
    private final Integer maximumIntegerValue;

    public IntegerValidationRule(){
        // no limits
        minimumIntegerValue = null;
        maximumIntegerValue = null;
    }

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
        if(minimumIntegerValue == null && maximumIntegerValue == null){
            return true;
        }
        return (intVal>=minimumIntegerValue && intVal<=maximumIntegerValue);
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
        String postfix = "";
        if(minimumIntegerValue!=null && maximumIntegerValue!=null){
            postfix = " of min %d and max %d".formatted(minimumIntegerValue, maximumIntegerValue);
        }
        return String.format("Value must be an Integer%s", postfix);
    }

    public Integer getMinimumIntegerValue() {
        return minimumIntegerValue;
    }

    public Integer getMaximumIntegerValue() {
        return maximumIntegerValue;
    }
}
