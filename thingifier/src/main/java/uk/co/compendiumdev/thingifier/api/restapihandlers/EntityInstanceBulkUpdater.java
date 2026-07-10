package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class EntityInstanceBulkUpdater {

    private final EntityDefinition entity;
    private final EntityInstance instance;

    public EntityInstanceBulkUpdater(EntityDefinition entity) {
        this.entity = entity;
        this.instance = null;
    }

    public EntityInstanceBulkUpdater(EntityInstance instance) {
        this.entity = instance.getEntity();
        this.instance = instance;
    }

    public EntityInstanceDraft setFieldValuesFrom(List<NamedValue> fieldValues) {

        if (instance != null) {
            final List<String> anyErrors = findAnyGuidOrIdDifferences(instance, fieldValues);
            if (anyErrors.size() > 0) {
                throw new RuntimeException(anyErrors.get(0));
            }
        }

        return setFieldValuesFromArgsIgnoring(
                fieldValues,
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
    }

    public EntityInstanceDraft setFieldValuesFromArgsIgnoring(
            List<NamedValue> fieldValues, final List<String> ignoreFields) {

        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entity);
        for (NamedValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
                draft.withField(entry.getName(), entry.asString());
            }
        }
        return draft;
    }

    public EntityInstanceDraft overrideFieldValuesFromArgsIgnoring(
            final List<NamedValue> fieldValues, final List<String> ignoreFields) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entity);
        for (NamedValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
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
        List<String> errorMessages = new java.util.ArrayList<>();

        for (NamedValue entry : fieldValues) {
            Field field = instance.getEntity().getField(entry.name);
            if (field == null
                    || (field.getType() != FieldType.AUTO_INCREMENT
                            && field.getType() != FieldType.AUTO_GUID)) {
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
}
