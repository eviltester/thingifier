package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class EntityInstance {

    // TODO: this is messy because of cloning and documentation - find a way to simplify

    private final EntityDefinition entityDefinition;
    private final InstanceFields instanceFields;

    // used internally to reference the instance, is not exposed to the world
    private final UUID internalId;

    EntityInstance(EntityDefinition eDefn) {
        this(eDefn, UUID.randomUUID());
    }

    EntityInstance(EntityDefinition eDefn, UUID internalId) {
        this(eDefn, internalId, eDefn.instantiateFields());
    }

    private EntityInstance(
            final EntityDefinition eDefn, final UUID internalId, final InstanceFields fields) {
        this.entityDefinition = eDefn;
        this.instanceFields = fields.cloned();
        this.internalId = internalId;
    }

    public static EntityInstance repositorySnapshot(
            final EntityDefinition entityDefinition,
            final UUID internalId,
            final InstanceFields fields) {
        return new EntityInstance(entityDefinition, internalId, fields);
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

    public FieldValue getFieldValue(String fieldName) {
        return instanceFields.getFieldValue(fieldName);
    }

    public EntityDefinition getEntity() {
        return this.entityDefinition;
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

    public ValidationReport validate() {
        return validateFields();
    }

    public List<FieldValue> getAssignedFieldValues() {
        return instanceFields.assignedValues();
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
