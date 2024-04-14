package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

import java.util.*;

public class EntityInstance {

    // TODO: this is messy because of cloning and documentation - find a way to simplify

    private final EntityInstanceRelationships relationships;
    private final EntityDefinition entityDefinition;
    private final InstanceFields instanceFields;

    // used internally to reference the instance, is not exposed to the world
    private final UUID internalId;

    public EntityInstance(EntityDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instanceFields = eDefn.instantiateFields();
        this.relationships = new EntityInstanceRelationships(this);
        internalId = UUID.randomUUID();
    }

    public EntityInstance addAutoGUIDstoInstance(){
        // allow GUIDs to be defined as being 'auto' in which case we will auto generate them
        List<Field> autoGuids = entityDefinition.getFieldsOfType(FieldType.AUTO_GUID);
        for(Field autoGuid : autoGuids){
            instanceFields.addValue(entityDefinition.getField(autoGuid.getName()).valueFor( UUID.randomUUID().toString()));
        }

        return this;
    }


    public void addAutoIncrementIdsToInstance(Map<String,AutoIncrement> autos) {

        for(Field autoIncrementedField : entityDefinition.getFieldsOfType(FieldType.AUTO_INCREMENT)){
            AutoIncrement auto = autos.get(autoIncrementedField.getName());
            if(!instanceFields.hasAssignedValue(autoIncrementedField.getName())){
                instanceFields.putValue(autoIncrementedField.getName(),
                        String.valueOf(auto.getNextValueAndUpdate()));
            }
        }
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
        output.append("\t\t\tInternal Ref: " + getInternalId() + "\n");
        //output.append(instance.toString() + "\n");
        for (String fieldName : entityDefinition.getFieldNames()) {
            FieldValue fieldValue = getFieldValue(fieldName);
            if(fieldValue!=null) {
                output.append(String.format("\t\t\t\t %s : %s %n", fieldName, fieldValue.asString()));
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
        if(entityDefinition.hasPrimaryKeyField()){
            return instanceFields.getFieldValue(entityDefinition.getPrimaryKeyField().getName()).asString();
        }

        // TODO: what should we do if a primary key has not been defined? return the first auto field? or null like this?
        return null;
    }

    public List<String> getFieldNames() {
        return this.entityDefinition.getFieldNames();
    }

    public EntityInstance setValue(String fieldName, String value) {
        instanceFields.setValue(fieldName, value);
        return this;
    }


    public EntityInstance overrideValue(final String key, final String value) {
        // bypass all validation - except, field must exist
        this.instanceFields.putValue(key, value);
        return this;
    }

    public FieldValue getFieldValue(String fieldName){
        return instanceFields.getFieldValue(fieldName);
    }


    public EntityDefinition getEntity() {
        return this.entityDefinition;
    }

    /**
     * connect this thing to another thing using the relationship relationshipName
     */
    public EntityInstanceRelationships getRelationships(){
        return relationships;
    }

    /*
        Validation
     */

    private ValidationReport validateFields(){
        return validateFieldValues(new ArrayList<>(), false);
    }

    public ValidationReport validateFieldValues(List<String> excluding, boolean amAllowedToSetIds){
        return instanceFields.validateFields(excluding, amAllowedToSetIds);
    }



    public ValidationReport validateRelationships(){
        return relationships.validateRelationships();
    }

    public ValidationReport validate() {
        ValidationReport report = new ValidationReport();

        report.combine(validateFields());
        report.combine(validateRelationships());

        return report;
    }

    // Cloning and documentation

    public void clearAllFields() {
        List<String>ignoreFields = new ArrayList<>();

        ignoreFields.addAll(getEntity().
                                getFieldNamesOfType(
                                    FieldType.AUTO_INCREMENT,
                                    FieldType.AUTO_GUID));

        instanceFields.deleteAllFieldValuesExcept(ignoreFields);
    }

    public EntityInstance createDuplicateWithoutRelationships() {
        EntityInstance cloneInstance = new EntityInstance(entityDefinition);

        for(String fieldName : instanceFields.getDefinition().getFieldNames()){
            FieldValue value = instanceFields.getAssignedValue(fieldName);
            if(value!=null){
                cloneInstance.instanceFields.addValue(value.cloned());
            }
        }

        return cloneInstance;
    }


    public InstanceFields getFields() {
        return instanceFields;
    }


    public boolean hasInstantiatedFieldNamed(String fieldName) {

        if(entityDefinition.hasFieldNameDefined(fieldName)) {
            return instanceFields.hasAssignedValue(fieldName);
        }

        return false;
    }

    public boolean hasFieldNamed(String fieldName) {
        return entityDefinition.hasFieldNameDefined(fieldName);
    }


}
