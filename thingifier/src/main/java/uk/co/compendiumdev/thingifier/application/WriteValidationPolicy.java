package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.BodyFieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class WriteValidationPolicy {

    private final ThingStore store;
    private final boolean enforceDeclaredTypes;

    WriteValidationPolicy(final ThingStore store, final boolean enforceDeclaredTypes) {
        this.store = store;
        this.enforceDeclaredTypes = enforceDeclaredTypes;
    }

    ThingCommandResult validateDeclaredFieldTypes(
            final EntityDefinition entity, final List<BodyFieldValue> bodyFields) {
        return validateDeclaredFieldTypesIgnoring(entity, bodyFields, new ArrayList<>());
    }

    ThingCommandResult validateDeclaredFieldTypesIgnoringProtected(
            final EntityDefinition entity, final List<BodyFieldValue> bodyFields) {
        if (entity == null) {
            return null;
        }
        return validateDeclaredFieldTypesIgnoring(
                entity,
                bodyFields,
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
    }

    List<NamedValue> normalizedFieldValues(
            final EntityDefinition entity,
            final List<NamedValue> fieldValues,
            final List<BodyFieldValue> bodyFields) {
        if (entity == null) {
            return fieldValues;
        }

        List<NamedValue> normalized = new ArrayList<>();
        for (NamedValue value : fieldValues) {
            BodyFieldValue bodyField = bodyFieldNamed(bodyFields, value.getName());
            Field field = entity.getField(value.getName());
            if (bodyField != null
                    && field != null
                    && (field.getType() == FieldType.INTEGER
                            || field.getType() == FieldType.AUTO_INCREMENT)
                    && bodyField.getSourceType() == BodyFieldValue.SourceType.NUMERIC) {
                normalized.add(
                        new NamedValue(value.getName(), integerString(bodyField.getValue())));
            } else {
                normalized.add(value);
            }
        }
        return normalized;
    }

    ThingCommandResult validateCreate(
            final EntityDefinition entity,
            final List<NamedValue> fieldValues,
            final boolean allowRequestedPrimaryKey,
            final String requestedPrimaryKey) {
        List<String> protectedFieldErrors = new ArrayList<>();
        for (String protectedField :
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID)) {
            if (containsField(fieldValues, protectedField) && !allowRequestedPrimaryKey) {
                protectedFieldErrors.add(
                        String.format("Not allowed to create with %s", protectedField));
            }
        }
        if (!protectedFieldErrors.isEmpty()) {
            return ThingCommandResult.error(
                    "Invalid Creation: " + String.join(", ", protectedFieldErrors));
        }

        if (allowRequestedPrimaryKey) {
            ThingCommandResult duplicateResult =
                    duplicateProtectedFieldError(entity, fieldValues, requestedPrimaryKey);
            if (duplicateResult != null) {
                return duplicateResult;
            }
        }
        return null;
    }

    ThingCommandResult validateReplaceCreate(
            final EntityDefinition entity,
            final String identifier,
            final List<NamedValue> fieldValues) {
        List<Field> generatedCreationFields =
                entity.getFieldsOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        if (!generatedCreationFields.isEmpty()) {
            return ThingCommandResult.error(
                    ApplicationError.replaceCreateAutoFieldsNotAllowed(
                            entity.getName(), fieldNames(generatedCreationFields)));
        }

        Field primaryKey = entity.getPrimaryKeyField();
        for (NamedValue namedValue : fieldValues) {
            if (namedValue.name.equals(primaryKey.getName())
                    && !namedValue.value.equals(identifier)) {
                return ThingCommandResult.error(
                        ApplicationError.replaceCreateKeyMismatch(
                                entity.getName(), identifier, namedValue.value));
            }
        }

        return null;
    }

    private ThingCommandResult validateDeclaredFieldTypesIgnoring(
            final EntityDefinition entity,
            final List<BodyFieldValue> bodyFields,
            final List<String> doNotValidateFields) {
        if (!enforceDeclaredTypes || entity == null) {
            return null;
        }

        List<String> errors = new ArrayList<>();
        for (BodyFieldValue fieldValue : bodyFields) {
            if (doNotValidateFields.contains(fieldValue.getName())) {
                continue;
            }

            Field field = entity.getField(fieldValue.getName());
            if (field == null) {
                continue;
            }

            String errorMessage =
                    String.format(
                            "%s should be %s but was %s",
                            field.getName(), field.getType(), fieldValue.sourceTypeDisplayName());

            if (field.getType() == FieldType.BOOLEAN
                    && fieldValue.getSourceType() != BodyFieldValue.SourceType.BOOLEAN) {
                errors.add(errorMessage);
            }
            if ((field.getType() == FieldType.INTEGER
                            || field.getType() == FieldType.AUTO_INCREMENT)
                    && fieldValue.getSourceType() != BodyFieldValue.SourceType.NUMERIC) {
                errors.add(errorMessage);
            }
            if (field.getType() == FieldType.FLOAT
                    && fieldValue.getSourceType() != BodyFieldValue.SourceType.NUMERIC) {
                errors.add(errorMessage);
            }
        }

        if (errors.isEmpty()) {
            return null;
        }
        return ThingCommandResult.error(String.join(", ", errors));
    }

    private ThingCommandResult duplicateProtectedFieldError(
            final EntityDefinition entity,
            final List<NamedValue> fieldValues,
            final String requestedPrimaryKey) {
        for (String fieldName :
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID)) {
            String value = valueFor(fieldValues, fieldName);
            if ((value == null || value.trim().isEmpty())
                    && entity.hasPrimaryKeyField()
                    && entity.getPrimaryKeyField().getName().equals(fieldName)) {
                value = requestedPrimaryKey;
            }
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            EntityInstance found = store.entityQueries().findByField(entity, fieldName, value);
            if (found != null) {
                return ThingCommandResult.error(
                        ApplicationError.conflict(
                                "Cannot Create with duplicate values: "
                                        + String.format(
                                                "Found Existing item with %s of %s",
                                                fieldName, value)));
            }
        }
        return null;
    }

    private boolean containsField(final List<NamedValue> fieldValues, final String fieldName) {
        return valueFor(fieldValues, fieldName) != null;
    }

    private String valueFor(final List<NamedValue> fieldValues, final String fieldName) {
        for (NamedValue value : fieldValues) {
            if (value.getName().equals(fieldName)) {
                return value.asString();
            }
        }
        return null;
    }

    private BodyFieldValue bodyFieldNamed(
            final List<BodyFieldValue> bodyFields, final String fieldName) {
        for (BodyFieldValue field : bodyFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    private String integerString(final String value) {
        return String.valueOf((int) Double.parseDouble(value));
    }

    private String fieldNames(final List<Field> fields) {
        String names = "";
        for (Field field : fields) {
            if (!names.isEmpty()) {
                names = names + ", ";
            }
            names = names + field.getName();
        }
        return names;
    }
}
