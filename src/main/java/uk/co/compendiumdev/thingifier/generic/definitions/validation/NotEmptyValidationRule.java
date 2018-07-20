package uk.co.compendiumdev.thingifier.generic.definitions.validation;

public class NotEmptyValidationRule implements ValidationRule {

    public boolean validates(String value) {
        if (value == null) {
            return false;
        }

        return value.trim().length() != 0;
    }

    @Override
    public String getErrorMessage(String fieldName) {
        return String.format("%s : can not be empty", fieldName);
    }
}
