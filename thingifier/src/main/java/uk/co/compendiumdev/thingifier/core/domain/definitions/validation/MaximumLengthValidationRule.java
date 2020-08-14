package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public class MaximumLengthValidationRule implements ValidationRule {
    private final int maxLength;

    public MaximumLengthValidationRule(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean validates(final FieldValue value) {

        final String stringValue = value.asString();

        if(stringValue==null){
            return true;
        }

        if(stringValue.length()>maxLength){
            return false;
        }

        return true;
    }

    @Override
    public String getErrorMessage(final FieldValue value) {
        return String.format(
                "Maximum allowable length exceeded for %s - maximum allowed is %d",
                value.getName(), maxLength);
    }
}
