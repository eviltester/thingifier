package uk.co.compendiumdev.thingifier.core.repository.validation;

import java.util.List;
import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.EntityDomainValidationContext;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.EntityDomainValidator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.GlobalValidationContext;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.GlobalValidator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.InstanceValidationContext;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.InstanceValidator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationOperation;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Runs the ordered write validation pipeline for materialized entity candidates.
 *
 * <p>The repository owns this pipeline because it has the final candidate instance: create IDs have
 * already been prepared, and amend/replace drafts have already been merged with the existing
 * instance. Each stage collects all errors within that stage and short-circuits before later stages
 * so expensive or stateful validators do not run against structurally invalid data.
 */
public final class EntityInstanceWriteValidator {

    private final EntityUniquenessChecker uniquenessChecker;
    private final Supplier<ERSchema> schemaSupplier;
    private final ThingStore store;

    /**
     * Creates a validator that can run field and uniqueness checks.
     *
     * <p>This constructor is retained for callers that do not have a store-backed schema context.
     * Entity domain and global validators are skipped when no schema/store is supplied.
     *
     * @param uniquenessChecker uniqueness checker for built-in unique field rules
     */
    public EntityInstanceWriteValidator(final EntityUniquenessChecker uniquenessChecker) {
        this(uniquenessChecker, () -> null, null);
    }

    /**
     * Creates a validator that can run the full field, instance, entity domain, and global
     * pipeline.
     *
     * @param uniquenessChecker uniqueness checker for built-in unique field rules
     * @param schemaSupplier supplier for the active schema at write time
     * @param store active store used by entity domain and global validators
     */
    public EntityInstanceWriteValidator(
            final EntityUniquenessChecker uniquenessChecker,
            final Supplier<ERSchema> schemaSupplier,
            final ThingStore store) {
        this.uniquenessChecker = uniquenessChecker;
        this.schemaSupplier = schemaSupplier;
        this.store = store;
    }

    /**
     * Validates a fully prepared create candidate and throws if invalid.
     *
     * @param instance candidate instance to validate
     */
    public void assertValidForCreate(final EntityInstance instance) {
        throwIfInvalid(validateForCreate(instance));
    }

    /**
     * Validates an amendment candidate where no existing instance context is available.
     *
     * @param instance candidate instance to validate
     */
    public void assertValidForAmendment(final EntityInstance instance) {
        throwIfInvalid(validateForAmendment(instance));
    }

    /**
     * Validates an amendment candidate with access to the persisted instance being changed.
     *
     * @param candidate materialized amendment candidate
     * @param existing existing persisted instance
     */
    public void assertValidForAmendment(
            final EntityInstance candidate, final EntityInstance existing) {
        throwIfInvalid(validateForAmendment(candidate, existing));
    }

    /**
     * Validates a replacement candidate with access to the persisted instance being replaced.
     *
     * @param candidate materialized replacement candidate
     * @param existing existing persisted instance
     */
    public void assertValidForReplacement(
            final EntityInstance candidate, final EntityInstance existing) {
        throwIfInvalid(validateForReplacement(candidate, existing));
    }

    /**
     * Runs the create validation pipeline.
     *
     * @param instance candidate instance to validate
     * @return validation report for the first failing stage, or a valid report
     */
    public ValidationReport validateForCreate(final EntityInstance instance) {
        return validateForWrite(instance, null, ValidationOperation.CREATE);
    }

    /**
     * Runs the amendment validation pipeline without persisted instance context.
     *
     * @param instance candidate instance to validate
     * @return validation report for the first failing stage, or a valid report
     */
    public ValidationReport validateForAmendment(final EntityInstance instance) {
        return validateForWrite(instance, null, ValidationOperation.AMEND);
    }

    /**
     * Runs the amendment validation pipeline with persisted instance context.
     *
     * @param candidate materialized amendment candidate
     * @param existing existing persisted instance
     * @return validation report for the first failing stage, or a valid report
     */
    public ValidationReport validateForAmendment(
            final EntityInstance candidate, final EntityInstance existing) {
        return validateForWrite(candidate, existing, ValidationOperation.AMEND);
    }

    /**
     * Runs the replacement validation pipeline with persisted instance context.
     *
     * @param candidate materialized replacement candidate
     * @param existing existing persisted instance
     * @return validation report for the first failing stage, or a valid report
     */
    public ValidationReport validateForReplacement(
            final EntityInstance candidate, final EntityInstance existing) {
        return validateForWrite(candidate, existing, ValidationOperation.REPLACE);
    }

    private ValidationReport validateForWrite(
            final EntityInstance instance,
            final EntityInstance existing,
            final ValidationOperation operation) {
        List<String> protectedFields =
                instance.getEntity()
                        .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        ValidationReport validation =
                instance.validateBuiltInFieldValuesForNormalWrite(protectedFields);
        if (!validation.isValid()) {
            return validation;
        }

        validation.combine(
                uniquenessChecker.checkFieldsForUniqueness(
                        instance, operation != ValidationOperation.CREATE));
        if (!validation.isValid()) {
            return validation;
        }

        validation.combine(instance.validateCustomFieldValues(List.of()));
        if (!validation.isValid()) {
            return validation;
        }

        validation.combine(validateInstanceRules(instance, existing, operation));
        if (!validation.isValid()) {
            return validation;
        }

        validation.combine(validateEntityDomainRules(instance, existing, operation));
        if (!validation.isValid()) {
            return validation;
        }

        validation.combine(validateGlobalRules(instance, existing, operation));
        return validation;
    }

    private ValidationReport validateInstanceRules(
            final EntityInstance instance,
            final EntityInstance existing,
            final ValidationOperation operation) {
        ValidationReport report = new ValidationReport();
        InstanceValidationContext context =
                new InstanceValidationContext(instance, existing, operation);
        for (InstanceValidator validator : instance.getEntity().instanceValidators()) {
            ValidationReport validation = validator.validate(context);
            if (validation != null) {
                report.combine(validation);
            }
        }
        return report;
    }

    private ValidationReport validateEntityDomainRules(
            final EntityInstance instance,
            final EntityInstance existing,
            final ValidationOperation operation) {
        ValidationReport report = new ValidationReport();
        ERSchema schema = schemaSupplier.get();
        if (schema == null || store == null) {
            return report;
        }

        EntityDomainValidationContext context =
                new EntityDomainValidationContext(schema, store, instance, existing, operation);
        for (EntityDomainValidator validator : instance.getEntity().domainValidators()) {
            ValidationReport validation = validator.validate(context);
            if (validation != null) {
                report.combine(validation);
            }
        }
        return report;
    }

    private ValidationReport validateGlobalRules(
            final EntityInstance instance,
            final EntityInstance existing,
            final ValidationOperation operation) {
        ValidationReport report = new ValidationReport();
        ERSchema schema = schemaSupplier.get();
        if (schema == null || store == null) {
            return report;
        }

        GlobalValidationContext context =
                new GlobalValidationContext(schema, store, instance, existing, operation);
        for (GlobalValidator validator : schema.globalValidators()) {
            ValidationReport validation = validator.validate(context);
            if (validation != null) {
                report.combine(validation);
            }
        }
        return report;
    }

    private void throwIfInvalid(final ValidationReport validation) {
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getCombinedErrorMessages());
        }
    }
}
