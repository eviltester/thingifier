package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

/**
 * Field-level custom validator for code-only checks on a single value.
 *
 * <p>Field validators run after Thingifier's built-in type, mandatory, object, and standard {@code
 * ValidationRule} checks have passed. They are intentionally code-only because arbitrary functions
 * cannot be safely exported to YAML or reassembled from model metadata.
 */
@FunctionalInterface
public interface FieldValidator {

    /**
     * Validates one field value.
     *
     * @param value candidate field value
     * @return validation report, or a valid report when the value is acceptable
     */
    ValidationReport validate(FieldValue value);
}
