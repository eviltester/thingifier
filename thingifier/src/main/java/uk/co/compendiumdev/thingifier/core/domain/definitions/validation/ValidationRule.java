package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

public interface ValidationRule {

    boolean validates(FieldValue value);

    String getErrorMessage(FieldValue value);
}
