package uk.co.compendiumdev.thingifier.core.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class MutableEntityInstance {

    private final EntityDefinition entity;
    private final UUID internalId;
    private final InstanceFields fields;

    private MutableEntityInstance(
            final EntityDefinition entity, final UUID internalId, final InstanceFields fields) {
        this.entity = entity;
        this.internalId = internalId;
        this.fields = fields;
    }

    public static MutableEntityInstance forEntity(final EntityDefinition entity) {
        return forEntity(entity, UUID.randomUUID());
    }

    public static MutableEntityInstance forEntity(
            final EntityDefinition entity, final UUID internalId) {
        return new MutableEntityInstance(entity, internalId, entity.instantiateFields());
    }

    public static MutableEntityInstance fromDraft(final EntityInstanceDraft draft) {
        return forEntity(draft.getEntity()).apply(draft);
    }

    public static EntityInstance snapshotFromDraft(final EntityInstanceDraft draft) {
        return fromDraft(draft).toEntityInstance();
    }

    public static MutableEntityInstance fromExisting(final EntityInstance instance) {
        MutableEntityInstance mutable =
                forEntity(instance.getEntity(), UUID.fromString(instance.getInternalId()));
        for (FieldValue value : instance.getAssignedFieldValues()) {
            mutable.fields.addValue(value);
        }
        return mutable;
    }

    public MutableEntityInstance patch(final EntityInstanceDraft draft) {
        return apply(draft);
    }

    public MutableEntityInstance replace(final EntityInstanceDraft draft) {
        clearAllFields();
        return apply(draft);
    }

    public MutableEntityInstance apply(final EntityInstanceDraft draft) {
        draft.validate();
        for (NamedValue value : draft.getFieldValues()) {
            setValue(value.getName(), value.asString());
        }
        for (NamedValue value : draft.getProtectedFieldValues()) {
            overrideValue(value.getName(), value.asString());
        }
        return this;
    }

    public MutableEntityInstance setValue(final String fieldName, final String value) {
        fields.setValue(fieldName, value);
        return this;
    }

    public MutableEntityInstance overrideValue(final String fieldName, final String value) {
        fields.putValue(fieldName, value);
        return this;
    }

    public MutableEntityInstance clearAllFields() {
        List<String> ignoreFields = new ArrayList<>();
        ignoreFields.addAll(
                entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
        fields.deleteAllFieldValuesExcept(ignoreFields);
        return this;
    }

    public boolean hasInstantiatedFieldNamed(final String fieldName) {
        if (entity.hasFieldNameDefined(fieldName)) {
            return fields.hasAssignedValue(fieldName);
        }
        return false;
    }

    public FieldValue getFieldValue(final String fieldName) {
        return fields.getFieldValue(fieldName);
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public String getInternalId() {
        return internalId.toString();
    }

    public String getPrimaryKeyValue() {
        if (entity.hasPrimaryKeyField()) {
            return fields.getFieldValue(entity.getPrimaryKeyField().getName()).asString();
        }
        return null;
    }

    public ValidationReport validateFieldValues(
            final List<String> excluding, final boolean allowedToSetIds) {
        return fields.validateFields(excluding, allowedToSetIds);
    }

    public EntityInstance toEntityInstance() {
        return EntityInstance.repositorySnapshot(entity, internalId, fields);
    }
}
