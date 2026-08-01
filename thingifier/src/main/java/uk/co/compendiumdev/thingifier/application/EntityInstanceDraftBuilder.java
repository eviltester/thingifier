package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class EntityInstanceDraftBuilder {

    private final EntityDefinition entity;
    private final EntityInstance instance;

    public EntityInstanceDraftBuilder(final EntityDefinition entity) {
        this.entity = entity;
        this.instance = null;
    }

    public EntityInstanceDraftBuilder(final EntityInstance instance) {
        this.entity = instance.getEntity();
        this.instance = instance;
    }

    public EntityInstanceDraft setFieldValuesFrom(final List<NamedValue> fieldValues) {
        if (instance != null) {
            final List<String> errors = findAnyGuidOrIdDifferences(instance, fieldValues);
            if (!errors.isEmpty()) {
                throw new RuntimeException(errors.get(0));
            }
        }

        return setFieldValuesFromArgsIgnoring(
                fieldValues,
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
    }

    public EntityInstanceDraft setFieldValuesFromArgsIgnoring(
            final List<NamedValue> fieldValues, final List<String> ignoreFields) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entity);
        for (NamedValue entry : fieldValues) {
            if (!ignoreFields.contains(entry.getName())) {
                draft.withField(entry.getName(), entry.asString());
            }
        }
        return draft;
    }

    public EntityInstanceDraft overrideFieldValuesFromArgsIgnoring(
            final List<NamedValue> fieldValues, final List<String> ignoreFields) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entity);
        for (NamedValue entry : fieldValues) {
            if (!ignoreFields.contains(entry.getName())) {
                if (isProtectedField(entry.getName())) {
                    draft.withProtectedField(entry.getName(), entry.asString());
                } else {
                    draft.withField(entry.getName(), entry.asString());
                }
            }
        }
        return draft;
    }

    private boolean isProtectedField(final String fieldName) {
        Field field = entity.getField(fieldName);
        return field != null
                && (field.getType() == FieldType.AUTO_INCREMENT
                        || field.getType() == FieldType.AUTO_GUID);
    }

    private List<String> findAnyGuidOrIdDifferences(
            final EntityInstance instance, final List<NamedValue> fieldValues) {
        List<String> errorMessages = new ArrayList<>();

        for (NamedValue entry : fieldValues) {
            Field field = instance.getEntity().getField(entry.name);
            if (field == null || !isIdentityField(instance, field)) {
                continue;
            }

            String existingValue = instance.getFieldValue(entry.name).asString();
            String entryValue = entry.value;
            if (field.getType() == FieldType.AUTO_INCREMENT) {
                try {
                    entryValue = String.valueOf((int) Float.parseFloat(entryValue));
                } catch (Exception e) {
                    // keep the original value for comparison
                }
            }
            if (existingValue != null
                    && !existingValue.trim().isEmpty()
                    && !existingValue.equalsIgnoreCase(entryValue)) {
                errorMessages.add(
                        String.format(
                                "Can not amend %s from %s to %s",
                                entry.name, existingValue, entryValue));
            }
        }

        return errorMessages;
    }

    private boolean isIdentityField(final EntityInstance instance, final Field field) {
        Field primaryKey = instance.getEntity().getPrimaryKeyField();
        return field.getType() == FieldType.AUTO_INCREMENT
                || field.getType() == FieldType.AUTO_GUID
                || (primaryKey != null && primaryKey.getName().equals(field.getName()));
    }
}
