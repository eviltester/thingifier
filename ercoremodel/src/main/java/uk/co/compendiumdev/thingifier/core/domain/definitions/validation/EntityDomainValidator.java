package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

/**
 * Entity-scoped validator that can inspect the wider domain.
 *
 * <p>Use this when the rule belongs to one entity type, but the decision needs more than the
 * candidate instance. The validator receives the active schema and store so it can compare the
 * candidate with other instances, relationships, or domain state.
 */
@FunctionalInterface
public interface EntityDomainValidator {

    /**
     * Validates one candidate write for the owning entity.
     *
     * @param context immutable context for the candidate write
     * @return validation report, or a valid report when the candidate is acceptable
     */
    ValidationReport validate(EntityDomainValidationContext context);
}
