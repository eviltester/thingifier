package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

/**
 * Entity instance validator for cross-field rules on one candidate instance.
 *
 * <p>Use this when all data needed to decide the rule is already on the candidate instance, for
 * example checking that an end date is after a start date. Use {@link EntityDomainValidator} when
 * the rule belongs to an entity but needs the active store or schema.
 */
@FunctionalInterface
public interface InstanceValidator {

    /**
     * Validates one candidate instance.
     *
     * @param context immutable context for the candidate write
     * @return validation report, or a valid report when the candidate is acceptable
     */
    ValidationReport validate(InstanceValidationContext context);
}
