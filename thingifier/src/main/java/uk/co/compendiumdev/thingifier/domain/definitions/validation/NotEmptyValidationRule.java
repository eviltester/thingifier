package uk.co.compendiumdev.thingifier.domain.definitions.validation;

public class NotEmptyValidationRule implements ValidationRule {

    public boolean validates(final String value) {
        if (value == null) {
            return false;
        }

        return value.trim().length() != 0;
    }

    @Override
    public String getErrorMessage(final String fieldName) {
        return String.format("%s : can not be empty", fieldName);
    }
}
