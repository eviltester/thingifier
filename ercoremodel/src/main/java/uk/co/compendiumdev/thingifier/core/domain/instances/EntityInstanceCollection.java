package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


final public class EntityInstanceCollection {

    private final EntityDefinition definition;
    private Map<String, EntityInstance> instances = new ConcurrentHashMap<>();

    // TODO: id's should be auto incremented at an instance collection level, not on the field definitions

    public EntityInstanceCollection(EntityDefinition thingDefinition) {
        this.definition = thingDefinition;
    }

    public EntityInstanceCollection(final EntityDefinition entity, final List<EntityInstance> instances) {
       this.definition=entity;
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

    public EntityInstanceCollection addInstance(EntityInstance instance) {

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

        instances.put(instance.getInternalId(), instance);
        return this;
    }

    /* create and add */
    // TODO: this looks like it was added to support testing, consider removing and adding to a test helper
    public EntityInstance createManagedInstance() {
        EntityInstance instance = new EntityInstance(definition);
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        addInstance(instance);
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

    @Deprecated // should be by named field
    public EntityInstance findInstanceByGUID(String instanceFieldValue) {

        // first - if it is not a GUID then dump it
        try{
            UUID.fromString(instanceFieldValue);
        }catch (IllegalArgumentException e){
            return null;
        }

        for(EntityInstance instance : instances.values()){
            if(instance.getGUID().equals(instanceFieldValue)){
                return instance;
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
            // TODO: could double check that the instance field "guid" matches
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
     *
     * @param guid
     * @return
     */
    public List<EntityInstance> deleteInstance(String guid) {

        if (!instances.containsKey(guid)) {
            throw new IndexOutOfBoundsException(
                    String.format("Could not find a %s with GUID %s",
                            definition.getName(), guid));
        }

        EntityInstance item = instances.get(guid);

        return deleteInstance(item);

    }

    public List<EntityInstance>  deleteInstance(EntityInstance anInstance) {

        if (!instances.containsValue(anInstance)) {
            throw new IndexOutOfBoundsException(
                    String.format("Could not find a %s with Internal GUID %s",
                            definition.getName(), anInstance.getInternalId()));
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

    private List<String> getGuidList() {
        List<String> guids = new ArrayList<>();
        for(EntityInstance instance : instances.values()){
            guids.add(instance.getInternalId());
        }

        return guids;
    }


    // todo: not comfortable with this method, we should be using specific field names
    // the API should know what fields to use as the primary reference for an entity instance
    @Deprecated
    public EntityInstance findInstanceByGUIDorID(final String instanceGuid) {
        EntityInstance instance = findInstanceByGUID(instanceGuid);
        if(instance==null){
            final List<Field> idFields = definition.getFieldsOfType(FieldType.AUTO_INCREMENT);
            if(!idFields.isEmpty()) {
                instance = findInstanceByFieldNameAndValue(
                                (idFields.get(0)).getName(),
                                instanceGuid);
            }
        }
        return instance;
    }


}
