package uk.co.compendiumdev.thingifier.core.domain.instances;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ERInstanceData {

    // The persistence mechanism EntityName, InstanceCollection
    private final ConcurrentHashMap<String, EntityInstanceCollection> instanceCollections;

    public ERInstanceData() {
        instanceCollections = new ConcurrentHashMap<>();
    }

    public String quoted(String aString){
        return "\"" + aString.replaceAll("\"", "\\\"") + "\"";
    }

    public String asJson(){

        StringBuilder dataArray = new StringBuilder();
        dataArray.append("{");


        // for each entity
        String separator = "";
        for( EntityInstanceCollection entry : instanceCollections.values()){
            EntityDefinition defn = entry.definition();

            dataArray.append( separator +  quoted(defn.getPlural()) + " : [");

            String instanceSeparator = "";
            for(EntityInstance instance : entry.getInstances()){
                dataArray.append( instanceSeparator + "{");

                String fieldSeparator = "";
                for(String fieldName : defn.getFieldNames()){
                    Field aField = defn.getField(fieldName);
                    if(instance.hasInstantiatedFieldNamed(fieldName)){
                        dataArray.append(fieldSeparator);
                        dataArray.append(quoted(aField.getName()) + ": " + instance.getFieldValue(fieldName).asJsonValue());
                    }else {
                        if (aField.isMandatory()) {
                            dataArray.append(fieldSeparator);
                            dataArray.append(quoted(aField.getName()) + ": " + aField.getDefaultValue().asJsonValue());
                        }
                    }

                    fieldSeparator = ", ";
                }

                dataArray.append("}");
                instanceSeparator = ", ";
            }


            dataArray.append("]");
            separator=", ";
        }

        dataArray.append("}");
        return dataArray.toString();
    }

    public ERInstanceData(final List<EntityInstance> instances) {
        instanceCollections = new ConcurrentHashMap<>();
        final EntityInstanceCollection managedInstances =
                createInstanceCollectionFor(instances.get(0).getEntity());
        managedInstances.addInstances(instances);
    }

    public EntityInstanceCollection createInstanceCollectionFor(
                                        final EntityDefinition definition) {
        EntityInstanceCollection aCollection = new EntityInstanceCollection(definition);
        instanceCollections.put(definition.getName(), aCollection);
        return aCollection;
    }

    public void createInstanceCollectionFrom(ERSchema schema) {
        for(EntityDefinition defn : schema.getEntityDefinitions()){
            createInstanceCollectionFor(defn);
        }
    }

    public List<EntityInstanceCollection> getAllInstanceCollections() {
        return new ArrayList<EntityInstanceCollection>(instanceCollections.values());
    }

    public EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        for (EntityInstanceCollection anInstanceCollection : instanceCollections.values()) {
            final List<String> guidFields = anInstanceCollection.definition().getFieldNamesOfType(FieldType.AUTO_GUID);
            for(String fieldName : guidFields){
                EntityInstance instance = anInstanceCollection.
                        findInstanceByFieldNameAndValue(fieldName, thingGUID);
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
                anInstanceCollection.deleteInstance(anEntityInstance);

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
        for (String instanceName : instanceCollections.keySet()) {
            clearInstanceDataFor(instanceName);
        }
    }

    public void clearInstanceDataFor(String instanceName) {
        EntityInstanceCollection instanceCollection = instanceCollections.get(instanceName);

        if(instanceCollection==null){
            return;
        }

        for(EntityInstance instance : instanceCollection.getInstances()) {
            deleteEntityInstance(instance);
        }
    }



}
