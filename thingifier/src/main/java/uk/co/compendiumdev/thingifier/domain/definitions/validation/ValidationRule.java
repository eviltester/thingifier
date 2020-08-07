package uk.co.compendiumdev.thingifier.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;

public interface ValidationRule {

    boolean validates(FieldValue value);

    String getErrorMessage(FieldValue value);
}
