package uk.co.compendiumdev.thingifier.core.domain.instances.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.TypeValidationFailedMessageGenerator;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class EntityInstanceDraftValidator {

    public void assertCanAddField(
            final EntityDefinition entityDefinition, final String name, final String value) {
        throwIfInvalid(validateFieldValue(entityDefinition, name, value, false));
    }

    public void assertCanAddProtectedField(
            final EntityDefinition entityDefinition, final String name, final String value) {
        throwIfInvalid(validateFieldValue(entityDefinition, name, value, true));
    }

    public void assertValid(final EntityInstanceDraft draft) {
        throwIfInvalid(validate(draft));
    }

    public ValidationReport validate(final EntityInstanceDraft draft) {
        ValidationReport validation = new ValidationReport();
        for (NamedValue value : draft.getFieldValues()) {
            validation.combine(
                    validateFieldValue(
                            draft.getEntity(), value.getName(), value.asString(), false));
        }
        for (NamedValue value : draft.getProtectedFieldValues()) {
            validation.combine(
                    validateFieldValue(draft.getEntity(), value.getName(), value.asString(), true));
        }
        return validation;
    }

    private ValidationReport validateFieldValue(
            final EntityDefinition entityDefinition,
            final String name,
            final String value,
            final boolean protectedWrite) {
        ValidationReport report = new ValidationReport();
        Field field = fieldForPath(entityDefinition, name);

        if (field == null) {
            report.setValid(false);
            report.addErrorMessage("Could not find field: " + displayName(name));
            return report;
        }

        if (protectedWrite && !isProtected(field)) {
            report.setValid(false);
            report.addErrorMessage(
                    "%s : field is not protected and should be set with withField".formatted(name));
            return report;
        }

        if (!protectedWrite && isProtected(field)) {
            report.setValid(false);
            report.addErrorMessage(
                    "%s : field is protected and can only be set with withProtectedField"
                            .formatted(name));
            return report;
        }

        if (value == null) {
            return report;
        }

        FieldValue fieldValue = FieldValue.is(field, value);
        try {
            report.combine(field.validate(fieldValue, protectedWrite));
            validateProtectedAutoIncrement(field, fieldValue, report);
            validateProtectedAutoGuid(field, fieldValue, report);
        } catch (IllegalArgumentException e) {
            report.setValid(false);
            report.addErrorMessage(
                    TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(
                            fieldValue, field.getType()));
        }

        return report;
    }

    private void validateProtectedAutoIncrement(
            final Field field, final FieldValue fieldValue, final ValidationReport report) {
        if (field.getType() != FieldType.AUTO_INCREMENT) {
            return;
        }

        try {
            fieldValue.asInteger();
        } catch (IllegalArgumentException e) {
            report.setValid(false);
            report.addErrorMessage(
                    TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(
                            fieldValue, FieldType.AUTO_INCREMENT));
        }
    }

    private void validateProtectedAutoGuid(
            final Field field, final FieldValue fieldValue, final ValidationReport report) {
        if (field.getType() != FieldType.AUTO_GUID) {
            return;
        }

        try {
            UUID.fromString(fieldValue.asString());
        } catch (IllegalArgumentException e) {
            report.setValid(false);
            report.addErrorMessage(
                    TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(
                            fieldValue, FieldType.AUTO_GUID));
        }
    }

    private Field fieldForPath(final EntityDefinition entityDefinition, final String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        List<String> path = new ArrayList<>(Arrays.asList(name.split("\\.", -1)));
        if (path.stream().anyMatch(String::isEmpty)) {
            return null;
        }

        String fieldName = path.remove(0);
        if (!entityDefinition.hasFieldNameDefined(fieldName)) {
            return null;
        }

        Field field = entityDefinition.getField(fieldName);
        while (!path.isEmpty()) {
            if (field.getType() != FieldType.OBJECT || field.getObjectDefinition() == null) {
                throw new IllegalArgumentException(
                        "Cannot reference fields on non object fields: " + field.getName());
            }

            DefinedFields objectDefinition = field.getObjectDefinition();
            fieldName = path.remove(0);
            if (!objectDefinition.hasFieldNameDefined(fieldName)) {
                throw new IllegalArgumentException("Could not find field: " + fieldName);
            }
            field = objectDefinition.getField(fieldName);
        }
        return field;
    }

    private boolean isProtected(final Field field) {
        return field.getType() == FieldType.AUTO_INCREMENT
                || field.getType() == FieldType.AUTO_GUID;
    }

    private void throwIfInvalid(final ValidationReport report) {
        if (!report.isValid()) {
            throw new IllegalArgumentException(report.getCombinedErrorMessages());
        }
    }

    private String displayName(final String name) {
        return name == null ? "" : name;
    }
}
