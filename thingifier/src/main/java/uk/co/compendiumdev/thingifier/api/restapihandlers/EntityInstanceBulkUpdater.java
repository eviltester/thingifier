package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceRepositoryAccess;

import java.util.List;

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
            final List<String> anyErrors =
                    EntityInstanceRepositoryAccess.findAnyGuidOrIdDifferences(instance, fieldValues);
            if(anyErrors.size()>0){
                throw new RuntimeException(anyErrors.get(0));
            }
        }

        return setFieldValuesFromArgsIgnoring(
                fieldValues,
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
    }

    public EntityInstanceDraft setFieldValuesFromArgsIgnoring(List<NamedValue> fieldValues,
                                               final List<String> ignoreFields) {

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

    public EntityInstanceDraft overrideFieldValuesFromArgsIgnoring(final List<NamedValue> fieldValues,
                                                    final List<String> ignoreFields) {
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
        return field != null &&
                (field.getType() == FieldType.AUTO_INCREMENT ||
                        field.getType() == FieldType.AUTO_GUID);
    }
}
