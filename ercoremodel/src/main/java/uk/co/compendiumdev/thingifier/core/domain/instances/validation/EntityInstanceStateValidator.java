package uk.co.compendiumdev.thingifier.core.domain.instances.validation;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class EntityInstanceStateValidator {

    public ValidationReport validate(final EntityInstance instance) {
        return validateFields(instance, new ArrayList<>(), false);
    }

    public ValidationReport validateFields(
            final EntityInstance instance,
            final List<String> excluding,
            final boolean allowedToSetIds) {
        return instance.validateFieldValues(excluding, allowedToSetIds);
    }
}
