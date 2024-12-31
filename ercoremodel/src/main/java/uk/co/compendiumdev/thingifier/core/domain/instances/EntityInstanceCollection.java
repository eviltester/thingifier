package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


final public class EntityInstanceCollection {

    private final EntityDefinition definition;
    private final Map<String, EntityInstance> instances = new ConcurrentHashMap<>();

    // id's should be auto incremented at an instance collection level, not on the field definitions
    private final Map<String, AutoIncrement> counters = new ConcurrentHashMap<>();

    public EntityInstanceCollection(EntityDefinition thingDefinition) {
        this.definition = thingDefinition;
        ensureCountersInitialized();
    }

    private void ensureCountersInitialized() {
        for(Field fieldDefn : definition.getFieldsOfType(FieldType.AUTO_GUID, FieldType.AUTO_INCREMENT)){
            if(fieldDefn.getType()==FieldType.AUTO_INCREMENT){
                createCounterFor(fieldDefn);
            }
        }
    }

    private AutoIncrement createCounterFor(Field fieldDefn) {
        if(!counters.containsKey(fieldDefn.getName())){
            AutoIncrement auto = new AutoIncrement(fieldDefn.getName(), fieldDefn.getDefaultValue().asInteger());
            counters.put(fieldDefn.getName(), auto);
            return auto;
        }else{
            return counters.get(fieldDefn.getName());
        }
    }

    public EntityInstanceCollection(final EntityDefinition entity, final List<EntityInstance> instances) {
       this.definition=entity;
       ensureCountersInitialized();
       addInstances(instances);
    }

    public EntityInstanceCollection addInstances(List<EntityInstance> addInstances) {

        if( definition.hasMaxInstanceLimit() &&
            ((instances.size() + addInstances.size()) > definition.getMaxInstanceLimit())){
                throw new RuntimeException(String.format(
                    "ERROR: Cannot add instances, would exceed maximum limit of %d",
                    definition.getMaxInstanceLimit()));
        }

        for(EntityInstance instance : addInstances){
            addInstance(instance);
        }

        return this;
    }

    public EntityInstance addInstance(EntityInstance instance) {

        if(instance.getEntity()!=definition){
            throw new RuntimeException(String.format(
                    "ERROR: Tried to add a %s instance to the %s",
                    instance.getEntity().getName(), definition.getName()));
        }

        if(definition.hasMaxInstanceLimit() && instances.size() >= definition.getMaxInstanceLimit()){
            throw new RuntimeException(String.format(
                    "ERROR: Cannot add instance, maximum limit of %d reached",
                    definition.getMaxInstanceLimit()
            ));
        }

        List<String> autoIncrementFieldsSet = new ArrayList<>();

        // if there are any AUTO_GUIDs or AUTO-INCREMENTs not set in the instance, then set them now
        for(Field fieldDefn : definition.getFieldsOfType(FieldType.AUTO_GUID, FieldType.AUTO_INCREMENT)){
            if(!instance.hasInstantiatedFieldNamed(fieldDefn.getName())){
                // set it here using the counter for the field
                if(fieldDefn.getType()==FieldType.AUTO_GUID){
                    instance.setValue(fieldDefn.getName(), UUID.randomUUID().toString());
                }
                if(fieldDefn.getType()==FieldType.AUTO_INCREMENT){
                    AutoIncrement counter = counters.get(fieldDefn.getName());
                    if(counter==null){
                        counter = createCounterFor(fieldDefn);
                    }
                    instance.overrideValue(fieldDefn.getName(),
                            String.valueOf(counter.getNextValueAndUpdate()));
                }
            }else{
                if(fieldDefn.getType()==FieldType.AUTO_INCREMENT) {
                    autoIncrementFieldsSet.add(fieldDefn.getName());
                }
            }
        }

        if(definition.hasPrimaryKeyField()){
            // check value of primary key exists and is unique
            Field primaryField = definition.getPrimaryKeyField();
            if(!instance.hasInstantiatedFieldNamed(primaryField.getName())){
                throw new RuntimeException(String.format(
                        "ERROR: Cannot add instance, primary key field %s not set",
                        primaryField.getName()));
            }

            for(EntityInstance existingInstance : instances.values()){
                if(existingInstance.getPrimaryKeyValue().equals(
                        instance.getPrimaryKeyValue()
                )){
                    throw new RuntimeException(String.format(
                            "ERROR: Cannot add instance, another instance with primary key value exists: %s",
                            existingInstance.getPrimaryKeyValue()));
                }
            }
        }

        instances.put(instance.getInternalId(), instance);

        for(String autoIncrementFieldSet : autoIncrementFieldsSet){
            // auto increment auto increments to above the value
            // should only do this if we actually add the item
            AutoIncrement counter = counters.get(autoIncrementFieldSet);
            if(counter.getCurrentValue() < instance.getFieldValue(autoIncrementFieldSet).asInteger()) {
                counter.incrementToNextAbove(instance.getFieldValue(autoIncrementFieldSet).asInteger());
            }
        }

        return instance;
    }

    public int countInstances() {
        return instances.size();
    }


    public EntityInstance findInstanceByFieldNameAndValue(String fieldName, String fieldValue) {

        if(fieldName==null) return null;
        if(fieldValue==null) return null;

        for (EntityInstance thing : instances.values()) {
            if(thing.hasFieldNamed(fieldName)) {
                if (thing.getFieldValue(fieldName).asString().contentEquals(fieldValue)) {
                    return thing;
                }
            }
        }

        return null;
    }


    public EntityInstance findInstanceByInternalID(String instanceFieldValue) {

        // first - if it is not a GUID then dump it
        try{
            UUID.fromString(instanceFieldValue);
        }catch (IllegalArgumentException e){
            return null;
        }

        if (instances.containsKey(instanceFieldValue)) {
            return instances.get(instanceFieldValue);
        }

        return null;
    }


    public Collection<EntityInstance> getInstances() {
        return instances.values();
    }


    /**
     * This deletes the instance but does not delete any mandatorily related items, these need to be handled by
     * another class using the returned list of alsoDelete, otherwise the model will be invalid
     */
    public List<EntityInstance> deleteInstance(String guid) {

        if (!instances.containsKey(guid)) {
            throw new IndexOutOfBoundsException(
                    String.format("unable to delete, could not find a %s with GUID %s",
                            definition.getName(), guid));
        }

        EntityInstance item = instances.get(guid);

        return deleteInstance(item);

    }

    public List<EntityInstance>  deleteInstance(EntityInstance anInstance) {

        if (!instances.containsValue(anInstance)) {
            throw new IndexOutOfBoundsException(
                    String.format("Unable to delete, could not find a %s with %s of %s",
                            definition.getName(),
                            definition.getPrimaryKeyField().getName(),
                            anInstance.getPrimaryKeyValue()));
        }

        instances.remove(anInstance.getInternalId());

        final List<EntityInstance> alsoDelete = anInstance.getRelationships().removeAllRelationships();

        return alsoDelete;
    }

    /*

        Definition abstractions

     */

    public EntityDefinition definition() {
        return definition;
    }


    public EntityInstance findInstanceByPrimaryKey(String primaryKeyValue) {
        for(EntityInstance instance : instances.values()){
            if(instance.getPrimaryKeyValue().equals(primaryKeyValue)){
                return instance;
            }
        }

        return null;
    }

    public Map<String, AutoIncrement> getCounters() {
        ensureCountersInitialized();
        return counters;
    }

    @Deprecated // todo: not sure this should exist - think it through, added for backwards compatibility when moving to AutoIncrement
    public void setNextIdCountersToAccomodate(List<NamedValue> fieldValues) {
    /*
        given a list of field values,
        if any of those match an id field
        then set our 'next id' for that field to above
        the value provided
     */
        ensureCountersInitialized();
        // todo: still have to handle nested objects - currently assume these are not ids, but they might be
        for(NamedValue fieldNameValue : fieldValues){
            final Field field = definition.getField(fieldNameValue.getName());
            if(field!=null && field.getType()== FieldType.AUTO_INCREMENT) {
                AutoIncrement auto = counters.get(field.getName());
                if(auto==null){
                    auto = createCounterFor(field);
                }
                auto.incrementToNextAbove(Integer.parseInt(fieldNameValue.value));
            }
        }

    }

    public ValidationReport checkFieldsForUniqueNess(EntityInstance instance, boolean isAmendment) {

        ValidationReport report = new ValidationReport();

        for(String fieldName : instance.getEntity().getFieldNames()){
            Field field = instance.getEntity().getField(fieldName);
            if(field.mustBeUnique()){
                String valueThatMustBeUnique = instance.getFieldValue(fieldName).asUniqueComparisonString();
                // check all instances to see if it is
                for(EntityInstance instanceToCheck : instances.values()){
                    FieldValue existingValue = instanceToCheck.getFieldValue(fieldName);
                    if(valueThatMustBeUnique.equals(existingValue.asUniqueComparisonString())){
                        // it is not
                        boolean dupeFound=true;
                        if(isAmendment){
                            if(instanceToCheck.getPrimaryKeyValue().equals(instance.getPrimaryKeyValue())){
                                // same item so ignore this one
                                dupeFound=false;
                            }
                        }
                        if(dupeFound) {
                            report.setValid(false);
                            report.addErrorMessage("Field %s Value is not unique".formatted(fieldName));
                            // we only need to find one to end the check
                            return report;
                        }
                    }
                }
            }
        }

        return report;
    }
}
