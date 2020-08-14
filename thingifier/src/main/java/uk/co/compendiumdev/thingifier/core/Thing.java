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

    private Thing(ThingDefinition thingDefinition) {
        this.definition = thingDefinition;
    }

    public static Thing create(String name, String plural) {
        Thing thing = new Thing(ThingDefinition.create(name, plural));
        return thing;
    }

    public ThingInstance createInstance() {
        return ThingInstance.create(definition);
    }

    public ThingInstance createInstance(String guid) {
        return ThingInstance.create(definition, guid);
    }

    public Thing addInstance(ThingInstance instance) {
        instances.put(instance.getGUID(), instance);
        return this;
    }

    /* create and add */
    public ThingInstance createManagedInstance() {
        final ThingInstance instance = ThingInstance.create(definition);
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

    public void withDefinedRelationship(final RelationshipVector fromVector) {
        definition().related().addRelationship(fromVector);
    }
}
