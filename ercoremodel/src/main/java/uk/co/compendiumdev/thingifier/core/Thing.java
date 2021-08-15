package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


final public class Thing {

    private final ThingDefinition definition;
    private Map<String, ThingInstance> instances = new ConcurrentHashMap<>();

    public Thing(ThingDefinition thingDefinition) {
        this.definition = thingDefinition;
    }

    public ThingInstance createInstance() {
        ThingInstance instance = new ThingInstance(definition);
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        return instance;
    }

    public ThingInstance createInstance(String guid) {
        ThingInstance instance = new ThingInstance(definition);
        instance.overrideValue("guid", guid);
        instance.addIdsToInstance();
        return instance;
    }

    public Thing addInstance(ThingInstance instance) {

        if(instance.getEntity()!=definition){
            throw new RuntimeException(String.format(
                    "ERROR: Tried to add a %s instance to the %s",
                    instance.getEntity().getName(), definition.getName()));
        }

        instances.put(instance.getGUID(), instance);
        return this;
    }

    /* create and add */
    public ThingInstance createManagedInstance() {
        ThingInstance instance = new ThingInstance(definition);
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        addInstance(instance);
        return instance;
    }

    public int countInstances() {
        return instances.size();
    }


    public ThingInstance findInstanceByField(FieldValue fieldValue) {

        for (ThingInstance thing : instances.values()) {
            if (thing.getFieldValue(fieldValue.getName())
                    .asString().contentEquals(fieldValue.asString())) {
                return thing;
            }
        }

        return null;
    }

    public ThingInstance findInstanceByGUID(String instanceFieldValue) {

        if (instances.containsKey(instanceFieldValue)) {
            return instances.get(instanceFieldValue);
        }

        return null;
    }


    public Collection<ThingInstance> getInstances() {
        return instances.values();
    }

    /**
     * Sorted list of instances
     */
    Comparator<ThingInstance> compareByInteger = new Comparator<ThingInstance>() {
        @Override
        public int compare(ThingInstance thing1, ThingInstance thing2) {
            int field1Value = thing1.getFieldValue("ID").asInteger();
            int field2Value = thing2.getFieldValue("ID").asInteger();
            return field2Value-field1Value;
        }
    };

    // TODO: make generic to allow sorting by any field
    public Collection<ThingInstance> getInstancesSortByID() {
        List<ThingInstance>sortedList = new ArrayList<>();
        sortedList.addAll(instances.values());

        // low to high sort
        Collections.sort(sortedList, compareByInteger.reversed());
        return sortedList;
    }



    /**
     * This deletes the instance but does not delete any mandatorily related items, these need to be handled by
     * another class using the returned list of alsoDelete, otherwise the model will be invalid
     *
     * @param guid
     * @return
     */
    public List<ThingInstance> deleteInstance(String guid) {

        if (!instances.containsKey(guid)) {
            throw new IndexOutOfBoundsException(
                    String.format("Could not find a %s with GUID %s",
                            definition.getName(), guid));
        }

        ThingInstance item = instances.get(guid);

        instances.remove(guid);

        final List<ThingInstance> alsoDelete = item.getRelationships().removeAllRelationships();


        return alsoDelete;
    }

    /*

        Definition abstractions

     */

    public ThingDefinition definition() {
        return definition;
    }

    private List<String> getGuidList() {
        List<String> guids = new ArrayList<>();
        for(ThingInstance instance : instances.values()){
            guids.add(instance.getGUID());
        }

        return guids;
    }


    // todo: not comfortable with this method, we should be using specific field names
    public ThingInstance findInstanceByGUIDorID(final String instanceGuid) {
        ThingInstance instance = findInstanceByGUID(instanceGuid);
        if(instance==null){
            final List<Field> idFields = definition.getFieldsOfType(FieldType.ID);
            if(!idFields.isEmpty()) {
                instance = findInstanceByField(
                        FieldValue.is(
                                (idFields.get(0)).getName(),
                                instanceGuid));
            }
        }
        return instance;
    }
}
