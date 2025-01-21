package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public class FloatValidationRule implements ValidationRule{

    private final Float minimumFloatValue;
    private final Float maximumFloatValue;

    public FloatValidationRule(){
        //allow creating without max and min validation
        this.minimumFloatValue = null;
        this.maximumFloatValue = null;
    }

    public FloatValidationRule(Float minimumFloatValue, Float maximumFloatValue){
        this.minimumFloatValue = minimumFloatValue;
        this.maximumFloatValue = maximumFloatValue;
    }

    private boolean validatesAgainstType(FieldValue value){
        try {
            float floatValue = value.asFloat();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean validates(FieldValue value){
        return validatesAgainstType(value) && withinAllowedFloatRange(value.asFloat());
    }

    private boolean withinAllowedFloatRange(final float floatValue) {
        if(minimumFloatValue == null || maximumFloatValue == null){
            return true;
        }
        return (floatValue>=minimumFloatValue &&
                floatValue<=maximumFloatValue);
    }

    public String getErrorMessage(FieldValue value){
        if(!validatesAgainstType(value)){
            return TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(value, FieldType.FLOAT);
        }
        return String.format(
                "%s : %s is not within range for type %s (%f to %f)",
                value.getName(),
                value.asString(),
                FieldType.FLOAT, minimumFloatValue, maximumFloatValue);
    }

    public String getExplanation(){
        String postfix = "";
        if(minimumFloatValue!=null){
            postfix = " of min %f and max %f".formatted(minimumFloatValue, maximumFloatValue);
        }
        return String.format("Value must be a Float%s", postfix);
    }

    public Float getMinimumFloatValue() {
        return minimumFloatValue;
    }

    public Float getMaximumFloatValue() {
        return maximumFloatValue;
    }

}
