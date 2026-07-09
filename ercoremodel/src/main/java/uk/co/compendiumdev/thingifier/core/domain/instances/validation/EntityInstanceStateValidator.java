package uk.co.compendiumdev.thingifier.core.domain.instances.validation;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceRepositoryAccess;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.ArrayList;
import java.util.List;

public final class EntityInstanceStateValidator {

    public ValidationReport validate(final EntityInstance instance) {
        ValidationReport report = validateFields(instance, new ArrayList<>(), false);
        report.combine(validateRelationships(instance));
        return report;
    }

    public ValidationReport validateFields(
            final EntityInstance instance,
            final List<String> excluding,
            final boolean allowedToSetIds) {
        return EntityInstanceRepositoryAccess.validateFieldValues(
                instance,
                excluding,
                allowedToSetIds);
    }

    public ValidationReport validateRelationships(final EntityInstance instance) {
        return EntityInstanceRepositoryAccess.validateRelationships(instance);
    }
}
