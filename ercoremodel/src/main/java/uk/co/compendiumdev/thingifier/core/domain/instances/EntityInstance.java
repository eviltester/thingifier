package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class EntityInstance {

    // TODO: this is messy because of cloning and documentation - find a way to simplify

    private final EntityInstanceRelationships relationships;
    private final EntityDefinition entityDefinition;
    private final InstanceFields instanceFields;

    // used internally to reference the instance, is not exposed to the world
    private final UUID internalId;
    private boolean repositoryOwned;

    EntityInstance(EntityDefinition eDefn) {
        this(eDefn, UUID.randomUUID());
    }

    EntityInstance(EntityDefinition eDefn, UUID internalId) {
        this.entityDefinition = eDefn;
        this.instanceFields = eDefn.instantiateFields();
        this.relationships = new EntityInstanceRelationships(this);
        this.internalId = internalId;
        this.repositoryOwned = false;
    }

    public static EntityInstance fromDraft(final EntityInstanceDraft draft) {
        return fromDraft(draft, UUID.randomUUID().toString());
    }

    private static EntityInstance fromDraft(
            final EntityInstanceDraft draft, final String internalId) {
        EntityInstance instance =
                new EntityInstance(draft.getEntity(), UUID.fromString(internalId));
        return instance.applyDraftFromRepository(draft);
    }

    EntityInstance applyDraftFromRepository(final EntityInstanceDraft draft) {
        draft.validate();
        for (NamedValue value : draft.getFieldValues()) {
            setValueFromRepository(value.getName(), value.asString());
        }
        for (NamedValue value : draft.getProtectedFieldValues()) {
            overrideValueFromRepository(value.getName(), value.asString());
        }
        return this;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
        output.append("\t\t\tInternal Ref: " + getInternalId() + "\n");
        // output.append(instance.toString() + "\n");
        for (String fieldName : entityDefinition.getFieldNames()) {
            FieldValue fieldValue = getFieldValue(fieldName);
            if (fieldValue != null) {
                output.append(
                        String.format("\t\t\t\t %s : %s %n", fieldName, fieldValue.asString()));
                if (entityDefinition.getField(fieldName).getType() == FieldType.OBJECT) {
                    output.append("\t\t\t\t\t\t" + fieldValue.asObject().toString());
                }
            }
        }

        output.append(relationships.toString());

        return output.toString();
    }

    public String getInternalId() {
        return internalId.toString();
    }

    public String getPrimaryKeyValue() {
        if (entityDefinition.hasPrimaryKeyField()) {
            return instanceFields
                    .getFieldValue(entityDefinition.getPrimaryKeyField().getName())
                    .asString();
        }

        // TODO: what should we do if a primary key has not been defined? return the first auto
        // field? or null like this?
        return null;
    }

    public List<String> getFieldNames() {
        return this.entityDefinition.getFieldNames();
    }

    EntityInstance setValue(String fieldName, String value) {
        ensureWritable();
        return setValueFromRepository(fieldName, value);
    }

    EntityInstance setValueFromRepository(String fieldName, String value) {
        instanceFields.setValue(fieldName, value);
        return this;
    }

    EntityInstance overrideValue(final String key, final String value) {
        ensureWritable();
        return overrideValueFromRepository(key, value);
    }

    EntityInstance overrideValueFromRepository(final String key, final String value) {
        // bypass all validation - except, field must exist
        this.instanceFields.putValue(key, value);
        return this;
    }

    public FieldValue getFieldValue(String fieldName) {
        return instanceFields.getFieldValue(fieldName);
    }

    public EntityDefinition getEntity() {
        return this.entityDefinition;
    }

    /** connect this thing to another thing using the relationship relationshipName */
    EntityInstanceRelationships getRelationships() {
        return relationships;
    }

    /*
       Validation
    */

    private ValidationReport validateFields() {
        return validateFieldValues(new ArrayList<>(), false);
    }

    public ValidationReport validateFieldValues(List<String> excluding, boolean amAllowedToSetIds) {
        return instanceFields.validateFields(excluding, amAllowedToSetIds);
    }

    public ValidationReport validateRelationships() {
        return relationships.validateRelationships();
    }

    public ValidationReport validate() {
        ValidationReport report = new ValidationReport();

        report.combine(validateFields());
        report.combine(validateRelationships());

        return report;
    }

    public boolean hasRelationshipInstances() {
        return relationships.hasAnyRelationshipInstances();
    }

    public Collection<EntityInstance> getRelatedItems(final String relationshipName) {
        return relationships.getConnectedItems(relationshipName);
    }

    // Cloning and documentation

    void clearAllFields() {
        ensureWritable();
        clearAllFieldsFromRepository();
    }

    void clearAllFieldsFromRepository() {
        List<String> ignoreFields = new ArrayList<>();

        ignoreFields.addAll(
                getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));

        instanceFields.deleteAllFieldValuesExcept(ignoreFields);
    }

    EntityInstance createDuplicateWithoutRelationships(final String internalId) {
        EntityInstance cloneInstance =
                new EntityInstance(entityDefinition, UUID.fromString(internalId));

        for (String fieldName : instanceFields.getDefinition().getFieldNames()) {
            FieldValue value = instanceFields.getAssignedValue(fieldName);
            if (value != null) {
                cloneInstance.instanceFields.addValue(value.cloned());
            }
        }

        return cloneInstance;
    }

    InstanceFields getFields() {
        return instanceFields;
    }

    void lockForRepository() {
        repositoryOwned = true;
    }

    void ensureWritable() {
        if (repositoryOwned) {
            throw new IllegalStateException(
                    "EntityInstance is repository-owned and read-only; use ThingRepository write APIs");
        }
    }

    public boolean hasInstantiatedFieldNamed(String fieldName) {

        if (entityDefinition.hasFieldNameDefined(fieldName)) {
            return instanceFields.hasAssignedValue(fieldName);
        }

        return false;
    }

    public boolean hasFieldNamed(String fieldName) {
        return entityDefinition.hasFieldNameDefined(fieldName);
    }
}
