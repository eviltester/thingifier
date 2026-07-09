package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

final class InMemoryEntityInstanceStore {

    // The persistence mechanism EntityName, InstanceCollection
    private final ConcurrentHashMap<String, InMemoryEntityInstanceCollection> instanceCollections;

    InMemoryEntityInstanceStore() {
        instanceCollections = new ConcurrentHashMap<>();
    }

    InMemoryEntityInstanceStore(final List<EntityInstance> instances) {
        instanceCollections = new ConcurrentHashMap<>();
        final InMemoryEntityInstanceCollection managedInstances =
                createInstanceCollectionFor(instances.get(0).getEntity());
        managedInstances.addInstances(instances);
    }

    InMemoryEntityInstanceCollection createInstanceCollectionFor(
            final EntityDefinition definition) {
        InMemoryEntityInstanceCollection aCollection =
                new InMemoryEntityInstanceCollection(definition);
        instanceCollections.put(definition.getName(), aCollection);
        return aCollection;
    }

    void createInstanceCollectionFrom(ERSchema schema) {
        for (EntityDefinition defn : schema.getEntityDefinitions()) {
            createInstanceCollectionFor(defn);
        }
    }

    List<InMemoryEntityInstanceCollection> getAllInstanceCollections() {
        return new ArrayList<InMemoryEntityInstanceCollection>(instanceCollections.values());
    }

    EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        for (InMemoryEntityInstanceCollection anInstanceCollection : instanceCollections.values()) {
            final List<String> guidFields =
                    anInstanceCollection.definition().getFieldNamesOfType(FieldType.AUTO_GUID);
            for (String fieldName : guidFields) {
                EntityInstance instance =
                        anInstanceCollection.findInstanceByFieldNameAndValue(fieldName, thingGUID);
                if (instance != null) {
                    return instance;
                }
            }
        }
        return null;
    }

    InMemoryEntityInstanceCollection getInstanceCollectionForEntityNamed(final String aName) {
        return instanceCollections.get(aName);
    }

    void deleteEntityInstance(final EntityInstance anEntityInstance) {
        // Delete only the stored instance. Repository implementations own relationship cleanup
        // and any mandatory-relationship cascade behavior.
        final InMemoryEntityInstanceCollection anInstanceCollection =
                instanceCollections.get(anEntityInstance.getEntity().getName());

        // there is no such entity definition named
        if (anInstanceCollection == null) {
            // if it was a hanging thing, not managed by EntityRelModel
            return;
        }

        anInstanceCollection.deleteInstance(anEntityInstance);
    }

    // TODO: this class only owns instance collections now. Repositories should decide whether
    // clearing an entity also needs relationship cleanup or cascade behavior.
    void clearAllData() {
        // clear all instance data
        for (String instanceName : instanceCollections.keySet()) {
            clearInstanceDataFor(instanceName);
        }
    }

    void clearInstanceDataFor(String instanceName) {
        InMemoryEntityInstanceCollection instanceCollection = instanceCollections.get(instanceName);

        if (instanceCollection == null) {
            return;
        }

        for (EntityInstance instance : new ArrayList<>(instanceCollection.getInstances())) {
            deleteEntityInstance(instance);
        }
    }
}
