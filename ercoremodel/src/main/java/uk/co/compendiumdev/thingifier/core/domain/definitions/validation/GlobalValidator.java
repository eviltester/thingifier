package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

/**
 * Schema-wide validator that sees every entity write.
 *
 * <p>Use this for truly global rules. If a rule belongs to one entity type but needs store/schema
 * access, use {@link EntityDomainValidator} instead so the model communicates the rule's owner.
 */
@FunctionalInterface
public interface GlobalValidator {

    /**
     * Validates one candidate write in the context of the whole schema.
     *
     * @param context immutable context for the candidate write
     * @return validation report, or a valid report when the candidate is acceptable
     */
    ValidationReport validate(GlobalValidationContext context);
}
