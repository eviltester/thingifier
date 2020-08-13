package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

import java.util.*;

public class ThingInstance {

    // TODO: this is messy because of cloning and documentation - find a way to simplify

    private final Relationships relationships;
    private final ThingDefinition entityDefinition;
    private final InstanceFields instanceFields;


    /**
     * example instance does not instantiate the ids or impact the
     * system management of instances
     *
     * @return
     */
    public static ThingInstance createExampleInstance(ThingDefinition eDefn){
        // ideally we don't want this thing instance to impact the ids
        // so don't set the ids and instead use the default and example values from definition
        return new ThingInstance(eDefn);
    }

    /**
     * Use the static factory methods to create the different variants
     *
     * @param eDefn
     */
    private ThingInstance(ThingDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instanceFields = eDefn.instantiateFields();
        this.relationships = new Relationships(this);
    }

    private void addGUIDtoInstance(){
        // todo: this adds a field called 'guid' but there may be other GUID fields,
        // allow GUIDs to be defined as being 'auto' in which case we will auto generate them
        instanceFields.addValue(FieldValue.is("guid", UUID.randomUUID().toString()));
    }

    private void addIdsToInstance() {
        instanceFields.addIdsToInstance();
    }

    static public ThingInstance create(ThingDefinition entityDefn, String guid){
        ThingInstance instance = new ThingInstance(entityDefn);
        instance.overrideValue("guid", guid);
        instance.addIdsToInstance();
        return instance;
    }

    static public ThingInstance create(ThingDefinition entityDefn){
        ThingInstance instance = new ThingInstance(entityDefn);
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        return instance;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
        //output.append(instance.toString() + "\n");
        for (String fieldName : entityDefinition.getFieldNames()) {
            output.append(String.format("\t\t\t\t %s : %s %n", fieldName, getFieldValue(fieldName).asString()));
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

    public ThingInstance setValue(String fieldName, String value) {
        instanceFields.setValue(fieldName, value);
        return this;
    }

    public ThingInstance setFieldValuesFrom(List<FieldValue> fieldValues) {


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

    public void overrideValue(final String key, final String value) {
        // bypass all validation - except, field must exist
        this.instanceFields.putValue(key, value);
    }

    public FieldValue getFieldValue(String fieldName){
        return instanceFields.getFieldValue(fieldName);
    }


    public ThingDefinition getEntity() {
        return this.entityDefinition;
    }

    /**
     * connect this thing to another thing using the relationship relationshipName
     */
    public Relationships getRelationships(){
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

    public ValidationReport validateNonProtectedFields() {
        return validateFieldValues(
                entityDefinition.getFieldNamesOfType(FieldType.ID, FieldType.GUID),
                    false);
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

    public ThingInstance createDuplicateWithoutRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition);

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
}
