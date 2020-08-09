package uk.co.compendiumdev.thingifier.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldValue;

public class NotEmptyValidationRule implements ValidationRule {

    public boolean validates(final FieldValue value) {

        final String stringValue = value.asString();

        if (stringValue == null) {
            return false;
        }

        return stringValue.trim().length() != 0;
    }

    @Override
    public String getErrorMessage(final FieldValue value) {
        return String.format("%s : can not be empty", value.getName());
    }
}
