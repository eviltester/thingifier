package uk.co.compendiumdev.thingifier.core.domain.instances;

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

    public EntityInstance(EntityDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instanceFields = eDefn.instantiateFields();
        this.relationships = new EntityInstanceRelationships(this);
    }

    public EntityInstance addGUIDtoInstance(){
        // todo: this adds a field called 'guid' but there may be other GUID fields,
        // allow GUIDs to be defined as being 'auto' in which case we will auto generate them
        instanceFields.addValue(FieldValue.is("guid", UUID.randomUUID().toString()));
        return this;
    }

    public EntityInstance addIdsToInstance() {
        instanceFields.addIdsToInstance();
        return this;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
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

    public String getGUID() {
        return instanceFields.getFieldValue("guid").asString();
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
                                    FieldType.ID,
                                    FieldType.GUID));

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


    /**
     *
     * Suspect these should not be in core and should be in the API handling
     *
     * We can have a setFieldValuesFrom and an overrideFieldValuesFrom
     * - but not the 'ignoring' lists
     *
     */

    public EntityInstance setFieldValuesFrom(List<FieldValue> fieldValues) {

        final List<String> anyErrors = instanceFields.findAnyGuidOrIdDifferences(fieldValues);
        if(anyErrors.size()>0){
            throw new RuntimeException(anyErrors.get(0));
        }

        setFieldValuesFromArgsIgnoring(fieldValues, entityDefinition.getFieldNamesOfType(FieldType.ID, FieldType.GUID));

        return this;
    }

    public void setFieldValuesFromArgsIgnoring(List<FieldValue> fieldValues,
                                               final List<String> ignoreFields) {

        for (FieldValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
                setValue(entry.getName(), entry.asString());
            }
        }
    }

    public void overrideFieldValuesFromArgsIgnoring(final List<FieldValue> fieldValues,
                                                    final List<String> ignoreFields) {
        for (FieldValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
                overrideValue(entry.getName(), entry.asString());
            }
        }
    }

}
