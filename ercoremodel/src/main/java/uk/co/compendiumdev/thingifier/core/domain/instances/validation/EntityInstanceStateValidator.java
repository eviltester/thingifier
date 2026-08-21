package uk.co.compendiumdev.thingifier.core.domain.instances.validation;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class EntityInstanceStateValidator {

    public ValidationReport validate(final EntityInstance instance) {
        return validateFieldsForNormalWrite(instance, new ArrayList<>());
    }

    /**
     * Validates persisted instance state using normal write rules.
     *
     * <p>The method name makes the protected-ID policy explicit at the call site and replaces the
     * older boolean flag that made caller intent difficult to read.
     *
     * @param instance instance to validate
     * @param excluding field names to skip during validation
     * @return validation report for field-level validation
     */
    public ValidationReport validateFieldsForNormalWrite(
            final EntityInstance instance, final List<String> excluding) {
        return instance.validateFieldValuesForNormalWrite(excluding);
    }

    /**
     * Validates persisted instance state while allowing protected ID fields.
     *
     * <p>This is for trusted repository/system paths that must validate a materialized object with
     * generated or restored protected identifiers already present.
     *
     * @param instance instance to validate
     * @param excluding field names to skip during validation
     * @return validation report for field-level validation
     */
    public ValidationReport validateFieldsForProtectedWrite(
            final EntityInstance instance, final List<String> excluding) {
        return instance.validateFieldValuesForProtectedWrite(excluding);
    }
}
