package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public class FloatValidationRule implements ValidationRule{

    private final Float minimumFloatValue;
    private final Float maximumFloatValue;

    public FloatValidationRule(Float minimumFloatValue, Float maximumFloatValue){
        this.minimumFloatValue = minimumFloatValue;
        this.maximumFloatValue = maximumFloatValue;
    }

    public boolean validates(FieldValue value){
        try {
            float floatValue = value.asFloat();
            return withinAllowedFloatRange(floatValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean withinAllowedFloatRange(final float floatValue) {
        return (floatValue>=minimumFloatValue &&
                floatValue<=maximumFloatValue);
    }

    public String getErrorMessage(FieldValue value){
        return String.format(
                "%s : %s is not within range for type %s (%f to %f)",
                value.getName(),
                value.asString(),
                FieldType.FLOAT, minimumFloatValue, maximumFloatValue);
    }

    public String getExplanation(){
        return String.format("Value must be a Float of min %f and max %f", minimumFloatValue, maximumFloatValue);
    }
}
