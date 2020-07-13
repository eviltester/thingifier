package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


final public class Thing {

    private final ThingDefinition definition;
    private Map<String, ThingInstance> instances = new ConcurrentHashMap<>();

    public Thing(ThingDefinition thingDefinition) {
        this.definition = thingDefinition;
    }


    // TODO should probably create a Thing with a populated definition rather than this way round
    public static Thing create(String name, String plural) {
        Thing thing = new Thing(ThingDefinition.create(name, plural));
        return thing;
    }

    public ThingInstance createInstance() {
        return new ThingInstance(definition);
    }

    public ThingInstance createInstance(String guid) {
        return new ThingInstance(definition, guid);
    }

    public Thing addInstance(ThingInstance instance) {

        instances.put(instance.getGUID(), instance);
        return this;
    }


    public int countInstances() {
        return instances.size();
    }


    public ThingInstance findInstanceByField(FieldValue fieldValue) {

        for (ThingInstance thing : instances.values()) {
            if (thing.getValue(fieldValue.getName()).contentEquals(fieldValue.getValue())) {
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

        final List<ThingInstance> alsoDelete = item.removeAllRelationships();


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


    public ThingInstance findInstanceByGUIDorID(final String instanceGuid) {
        ThingInstance instance = findInstanceByGUID(instanceGuid);
        if(instance==null){
            if(definition.hasIDField()) {
                instance = findInstanceByField(
                        FieldValue.is(
                                definition.getIDField().getName(),
                                instanceGuid));
            }
        }
        return instance;
    }
}
