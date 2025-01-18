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

    public boolean validates(FieldValue value){
        try {

            // integers can come in from JSON as doubles
            BigDecimal intFloatValue = new BigDecimal(value.asString());

            BigDecimal fractionalPart = intFloatValue.abs().subtract(new BigDecimal(intFloatValue.abs().toBigInteger()));

            if(!(fractionalPart.equals(new BigDecimal("0")) || fractionalPart.equals(new BigDecimal("0.0")))){
                throw new NumberFormatException();
            }

            int intVal = intFloatValue.intValue();

            return withinAllowedIntegerRange(intVal);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean withinAllowedIntegerRange(final int intVal) {
        return (intVal>=minimumIntegerValue &&
                intVal<=maximumIntegerValue);
    }

    public String getErrorMessage(FieldValue value){
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
