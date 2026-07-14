package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ThingDraftFactory {

    private final ThingStore store;

    ThingDraftFactory(final ThingStore store) {
        this.store = store;
    }

    EntityInstanceDraft createDraft(
            final EntityDefinition entity,
            final String requestedPrimaryKey,
            final List<NamedValue> fieldValues) {
        EntityInstanceDraft baseDraft = EntityInstanceDraft.forEntity(entity);
        if (requestedPrimaryKey != null
                && !requestedPrimaryKey.isEmpty()
                && entity.hasPrimaryKeyField()) {
            Field primaryKeyField = entity.getPrimaryKeyField();
            if (primaryKeyField.getType() == FieldType.AUTO_INCREMENT
                    || primaryKeyField.getType() == FieldType.AUTO_GUID) {
                baseDraft.withProtectedField(primaryKeyField.getName(), requestedPrimaryKey);
            } else {
                baseDraft.withField(primaryKeyField.getName(), requestedPrimaryKey);
            }
        }

        List<NamedValue> values = new ArrayList<>(fieldValues);
        if (requestedPrimaryKey != null && !requestedPrimaryKey.isEmpty()) {
            store.administration().accommodateProtectedIds(entity, values);
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(entity)
                            .overrideFieldValuesFromArgsIgnoring(
                                    values, entity.getFieldNamesOfType(FieldType.AUTO_GUID));
            copyBaseDraftValues(baseDraft, draft);
            return draft;
        }

        return new EntityInstanceDraftBuilder(entity).setFieldValuesFrom(values);
    }

    private void copyBaseDraftValues(
            final EntityInstanceDraft baseDraft, final EntityInstanceDraft draft) {
        for (NamedValue value : baseDraft.getFieldValues()) {
            draft.withField(value.getName(), value.asString());
        }
        for (NamedValue protectedValue : baseDraft.getProtectedFieldValues()) {
            draft.withProtectedField(protectedValue.getName(), protectedValue.asString());
        }
    }
}
