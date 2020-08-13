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
        // so don't set the ids and instead use the default values
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

        // use the static factory methods to create the different variants
//        addGUIDtoInstance();
//        addIdsToInstance();
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

//    public ThingInstance(ThingDefinition entityTestSession, String guid) {
//        this(entityTestSession);
//        instanceFields.addValue(FieldValue.is("guid", guid));
//    }

    static public ThingInstance create(ThingDefinition entityDefn){
        ThingInstance instance = new ThingInstance(entityDefn);
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        return instance;
    }

//    public ThingInstance(ThingDefinition eDefn) {
//        this(eDefn, true);
//    }


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

    public ThingInstance setFieldValuesFrom(final List<Map.Entry<String, String>> args) {

        final List<String> anyErrors = instanceFields.findAnyGuidOrIdDifferences(args);
        if(anyErrors.size()>0){
            throw new RuntimeException(anyErrors.get(0));
        }

        setFieldValuesFromArgsIgnoring(args, entityDefinition.getFieldNamesOfType(FieldType.ID, FieldType.GUID));

        return this;
    }

    public void setFieldValuesFromArgsIgnoring(final List<Map.Entry<String, String>> args,
                                                final List<String> ignoreFields) {

        for (Map.Entry<String, String> entry : args) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getKey())) {
                // set the value because it is not protected
                setValue(entry.getKey(), entry.getValue());
            }
        }
    }

    public void overrideFieldValuesFromArgsIgnoring(final List<Map.Entry<String, String>> args,
                                               final List<String> ignoreFields) {

        for (Map.Entry<String, String> entry : args) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getKey())) {
                // set the value because it is not protected
                overrideValue(entry.getKey(), entry.getValue());
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


    public boolean hasIDField() {
        return !entityDefinition.getFieldsOfType(FieldType.ID).isEmpty();
    }

    // todo assumes that there is one id field - should allow more
    public String getID() {
        String value = "";
        final List<Field> idFields = entityDefinition.getFieldsOfType(FieldType.ID);
        Field field = null;
        if(!idFields.isEmpty()){
            field = idFields.get(0);
        }
        if(field!=null) {
            value = getFieldValue(field.getName()).asString();
        }
        return value;
    }

    /**
     * connect this thing to another thing using the relationship relationshipName
     */
    public void connects(String relationshipName, ThingInstance thing) {
        relationships.connect( relationshipName, thing);
    }

    protected void isNowRelatedVia(RelationshipInstance relationship) {
        // if the relationship vector has a parent that is both ways then we need to create a relationship of the reverse type to the thing that called us
        if (relationship.getRelationship().isTwoWay()) {
            relationships.add(relationship);
        }
    }

    public ThingDefinition typeOfConnectedItems(String relationshipName) {
        return relationships.getTypeOfConnectedItems(relationshipName);
    }

    public Collection<ThingInstance> connectedItems(String relationshipName) {
        return relationships.getConnectedItems(relationshipName);
    }

    public void removeRelationshipsTo(ThingInstance thing, String relationshipName) {
        relationships.removeRelationshipsTo(thing, relationshipName);

    }

    public List<ThingInstance> removeAllRelationships() {
        return relationships.removeAllRelationships();
    }

    public void isNoLongerRelatedVia(RelationshipInstance relationship) {
        // delete any relationship to or from
        relationships.remove(relationship);
    }

    public void removeRelationshipsInvolvingMe(ThingInstance thing) {
        relationships.removeAllRelationshipsInvolving(thing);
    }

    public List<ThingInstance> connectedItemsOfType(String type) {
        return relationships.connectedItemsOfType(type);
    }

    public boolean hasAnyRelationshipInstances() {
        return relationships.hasAnyRelationshipInstances();

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

    public ThingInstance setCloneFieldValuesFrom(final InstanceFields args) {

        instanceFields.setValuesFromClone(args);

        return this;
    }

    public ThingInstance createDuplicateWithoutRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition);
        cloneInstance.setCloneFieldValuesFrom(instanceFields.cloned());
        return cloneInstance;
    }

    // this was only used in the documentation,
    // and the relationships were not used
    // and even if they were, there would not be any because the instance is 'blank'
//    public ThingInstance createDuplicateWithRelationships() {
//        ThingInstance cloneInstance = new ThingInstance(entityDefinition);
//        cloneInstance.setCloneFieldValuesFrom(instanceFields.cloned());
//        cloneInstance.setCloneRelationships(relationships.createClonedRelationships());
//        return cloneInstance;
//    }

//    private void setCloneRelationships(final List<RelationshipInstance> clonedRelationships) {
//        for(RelationshipInstance relationship : clonedRelationships){
//            relationships.add(relationship);
//        }
//    }

    public InstanceFields getFields() {
        return instanceFields;
    }
}
