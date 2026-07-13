package uk.co.compendiumdev.thingifier.core.repository.validation;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

@FunctionalInterface
public interface EntityUniquenessChecker {

    ValidationReport checkFieldsForUniqueness(EntityInstance instance, boolean isAmendment);
}
