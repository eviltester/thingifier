package uk.co.compendiumdev.thingifier.generic.definitions.validation;

public class MatchesTypeValidationRule implements ValidationRule {

    @Override
    public boolean validates(final String value) {

        // special case, handled by instance
        return false;
    }

    @Override
    public String getErrorMessage(final String fieldName) {
        return "Value must match defined type";
    }
}
