package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Thing {

    private final ThingDefinition definition;
    private Map<String, ThingInstance> instances = new HashMap<String, ThingInstance>();

    public Thing(ThingDefinition thingDefinition) {
        this.definition = thingDefinition;
    }



    // TODO should probably create a Thing with a populated definition rather than this way round
    public static Thing create(String name, String plural) {
        Thing thing = new Thing(ThingDefinition.create(name, plural));
        return thing;
    }

    public ThingInstance createInstance(){
        return new ThingInstance(definition);
    }

    public ThingInstance createInstance(String guid){
        return new ThingInstance(definition, guid);
    }

    public Thing addInstance(ThingInstance instance){

        instances.put(instance.getGUID(), instance);
        return this;
    }


    public int countInstances() {
        return instances.size();
    }



    public ThingInstance findInstance(FieldValue fieldValue) {

        for(ThingInstance thing : instances.values()){
            if(thing.getValue(fieldValue.getName()).contentEquals(fieldValue.getValue())){
                return thing;
            };
        }

        return null;
    }

    public ThingInstance findInstance(String instanceFieldValue) {

        if(instances.containsKey(instanceFieldValue)){
            return instances.get(instanceFieldValue);
        }

        return null;
    }

    public Collection<ThingInstance> getInstances() {
        return instances.values();
    }




    public Thing getCopyWithoutInstances() {
        Thing copyWithoutInstances = new Thing(definition);
        return copyWithoutInstances;
    }

    public Thing deleteInstance(String guid) {

        if(!instances.containsKey(guid)){
            throw new IndexOutOfBoundsException(
                    String.format("Could not find a %s with GUID %s",
                            definition.getName(), guid));
        }

        ThingInstance item = instances.get(guid);

        instances.remove(guid);

        item.tellRelatedItemsIAmDeleted();


        return this;
    }

    /*

        Definition abstractions

     */

    public ThingDefinition definition() {
        return definition;
    }



}
