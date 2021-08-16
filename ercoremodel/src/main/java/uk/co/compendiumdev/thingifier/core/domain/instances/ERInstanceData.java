package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ERInstanceData {
    private final ERSchema schema;
    private final ConcurrentHashMap<String, EntityInstanceCollection> instanceCollections;

    public ERInstanceData(final ERSchema schema) {
        this.schema = schema;
        instanceCollections = new ConcurrentHashMap<>();
    }

    public EntityInstanceCollection createInstanceCollectionFor(
                                        final EntityDefinition definition) {
        EntityInstanceCollection aCollection = new EntityInstanceCollection(definition);
        instanceCollections.put(definition.getName(), aCollection);
        return aCollection;
    }

    public List<EntityInstanceCollection> getAllInstanceCollections() {
        return new ArrayList<EntityInstanceCollection>(instanceCollections.values());
    }

    public EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        for (EntityInstanceCollection anInstanceCollection : instanceCollections.values()) {
            final List<String> guidFields = anInstanceCollection.definition().getFieldNamesOfType(FieldType.GUID);
            for(String fieldName : guidFields){
                EntityInstance instance = anInstanceCollection.
                        findInstanceByField(FieldValue.is(fieldName, thingGUID));
                if (instance != null) {
                    return instance;
                }
            }
        }
        return null;
    }

    public EntityInstanceCollection getInstanceCollectionForEntityNamed(final String aName) {
        return instanceCollections.get(aName);
    }

    public void deleteEntityInstance(final EntityInstance anEntityInstance) {
        // delete a thing and all related things with mandatory relationships
        final EntityInstanceCollection anInstanceCollection =
                    instanceCollections.get(anEntityInstance.getEntity().getName());

        // there is no such entity definition named
        if(anInstanceCollection==null){
            // if it was a hanging thing, not managed by EntityRelModel
            return;
        }

        // we may also have to delete things which are mandatorily related i.e. can't exist on their own
        final List<EntityInstance> otherInstancesToDelete =
                anInstanceCollection.deleteInstance(anEntityInstance.getGUID());

        // TODO: Warning recursion with no 'cut off' if any cyclical relationships then this might fail
        for(EntityInstance deleteMe : otherInstancesToDelete){
            deleteEntityInstance(deleteMe);
        }
    }

    //TODO: couldn't this be simpler, if we just clear all the collections then
    // all instances and relationships would be cleared? Why recurse individually
    // through them all?
    public void clearAllData() {
        // clear all instance data
        for (EntityInstanceCollection instanceCollection : instanceCollections.values()) {
            for(EntityInstance instance : instanceCollection.getInstances()) {
                deleteEntityInstance(instance);
            }
        }
    }
}
