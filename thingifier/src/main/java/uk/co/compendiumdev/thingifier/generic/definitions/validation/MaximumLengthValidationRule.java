package uk.co.compendiumdev.thingifier.generic.definitions.validation;

public class MaximumLengthValidationRule implements ValidationRule {
    private final int maxLength;

    public MaximumLengthValidationRule(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean validates(final String value) {
        if(value==null){
            return true;
        }
        if(value.length()>maxLength){
            return false;
        }

        return true;
    }

    @Override
    public String getErrorMessage(final String fieldName) {
        return String.format(
                "Maximum allowable length exceeded for %s - maximum allowed is %d",
                fieldName, maxLength);
    }
}
