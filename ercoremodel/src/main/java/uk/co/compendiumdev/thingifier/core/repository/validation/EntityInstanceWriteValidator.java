package uk.co.compendiumdev.thingifier.core.repository.validation;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class EntityInstanceWriteValidator {

    private final EntityUniquenessChecker uniquenessChecker;

    public EntityInstanceWriteValidator(final EntityUniquenessChecker uniquenessChecker) {
        this.uniquenessChecker = uniquenessChecker;
    }

    public void assertValidForCreate(final EntityInstance instance) {
        throwIfInvalid(validateForCreate(instance));
    }

    public void assertValidForAmendment(final EntityInstance instance) {
        throwIfInvalid(validateForAmendment(instance));
    }

    public ValidationReport validateForCreate(final EntityInstance instance) {
        return validateForWrite(instance, false);
    }

    public ValidationReport validateForAmendment(final EntityInstance instance) {
        return validateForWrite(instance, true);
    }

    private ValidationReport validateForWrite(
            final EntityInstance instance, final boolean isAmendment) {
        List<String> protectedFields =
                instance.getEntity()
                        .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        ValidationReport validation = instance.validateFieldValues(protectedFields, false);
        validation.combine(uniquenessChecker.checkFieldsForUniqueness(instance, isAmendment));
        return validation;
    }

    private void throwIfInvalid(final ValidationReport validation) {
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getCombinedErrorMessages());
        }
    }
}
