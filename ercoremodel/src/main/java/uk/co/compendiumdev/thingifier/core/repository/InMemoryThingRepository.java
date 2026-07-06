package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListFilter;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListSorter;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InMemoryThingRepository implements ThingRepository {

    private final String databaseKey;
    private final ERInstanceData instanceData;

    public InMemoryThingRepository(final String databaseKey) {
        this(databaseKey, new ERInstanceData());
    }

    public InMemoryThingRepository(final String databaseKey, final ERInstanceData instanceData) {
        this.databaseKey = databaseKey;
        this.instanceData = instanceData;
    }

    @Override
    public String databaseKey() {
        return databaseKey;
    }

    @Override
    public void initializeFrom(final ERSchema schema) {
        refreshSchema(schema);
    }

    @Override
    public void refreshSchema(final ERSchema schema) {
        createInstanceCollectionsFrom(schema);
    }

    @Override
    public ERInstanceData getInstanceData() {
        return instanceData;
    }

    @Override
    public EntityInstanceCollection createInstanceCollectionFor(final EntityDefinition definition) {
        EntityInstanceCollection existing = getInstanceCollectionForEntityNamed(definition.getName());
        if (existing != null) {
            return existing;
        }
        return instanceData.createInstanceCollectionFor(definition);
    }

    @Override
    public void createInstanceCollectionsFrom(final ERSchema schema) {
        for (EntityDefinition defn : schema.getEntityDefinitions()) {
            createInstanceCollectionFor(defn);
        }
    }

    @Override
    public List<EntityInstanceCollection> getAllInstanceCollections() {
        return instanceData.getAllInstanceCollections();
    }

    @Override
    public EntityInstanceCollection getInstanceCollectionForEntityNamed(final String entityName) {
        return instanceData.getInstanceCollectionForEntityNamed(entityName);
    }

    @Override
    public EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        return instanceData.findEntityInstanceByGUID(thingGUID);
    }

    @Override
    public EntityInstance findInstanceByPrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByPrimaryKey(primaryKeyValue);
    }

    @Override
    public EntityInstance findInstanceByFieldNameAndValue(
            final EntityDefinition entity, final String fieldName, final String fieldValue) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByFieldNameAndValue(fieldName, fieldValue);
    }

    @Override
    public Collection<EntityInstance> listInstances(final EntityDefinition entity) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return List.of();
        }
        return collection.getInstances();
    }

    @Override
    public List<EntityInstance> listInstances(
            final EntityDefinition entity, final QueryFilterParams queryParams) {
        List<EntityInstance> instances = new ArrayList<>(listInstances(entity));
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;
        instances = new EntityInstanceListFilter(params).filter(instances);
        return new EntityInstanceListSorter(params).sort(instances);
    }

    @Override
    public EntityInstance findInstanceByQueryIdentifier(
            final EntityDefinition entity, final String identifier) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }

        for (EntityInstance instance : collection.getInstances()) {
            if (matchesQueryIdentifier(instance, identifier)) {
                return instance;
            }
        }
        return null;
    }

    @Override
    public EntityInstance addInstance(final EntityInstance instance) {
        getInstanceCollectionForEntityNamed(instance.getEntity().getName()).addInstance(instance);
        return instance;
    }

    @Override
    public EntityInstance updateInstance(final EntityInstance instance) {
        return instance;
    }

    @Override
    public void deleteEntityInstance(final EntityInstance instance) {
        instanceData.deleteEntityInstance(instance);
    }

    @Override
    public void clearAllData() {
        instanceData.clearAllData();
    }

    @Override
    public void clearInstanceDataFor(final String entityName) {
        instanceData.clearInstanceDataFor(entityName);
    }

    @Override
    public ValidationReport checkFieldsForUniqueNess(
            final EntityInstance instance, final boolean isAmendment) {
        return getInstanceCollectionForEntityNamed(instance.getEntity().getName()).
                checkFieldsForUniqueNess(instance, isAmendment);
    }

    @Override
    public Map<String, AutoIncrement> countersFor(final EntityDefinition entity) {
        return getInstanceCollectionForEntityNamed(entity.getName()).getCounters();
    }

    @Override
    public void setNextIdCountersToAccomodate(
            final EntityDefinition entity, final List<NamedValue> fieldValues) {
        getInstanceCollectionForEntityNamed(entity.getName()).
                setNextIdCountersToAccomodate(fieldValues);
    }

    @Override
    public void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        from.getRelationships().connect(relationshipName, to);
    }

    @Override
    public List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        return parent.getRelationships().removeRelationshipsInvolving(child, relationshipName);
    }

    @Override
    public List<EntityInstance> removeAllRelationships(final EntityInstance instance) {
        return instance.getRelationships().removeAllRelationships();
    }

    @Override
    public Collection<EntityInstance> getConnectedItems(
            final EntityInstance instance, final String relationshipName) {
        return instance.getRelationships().getConnectedItems(relationshipName);
    }

    @Override
    public List<EntityInstance> listRelatedInstances(
            final EntityInstance instance,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;
        List<EntityInstance> instances = new ArrayList<>(
                getConnectedItems(instance, relationshipName));
        instances = new EntityInstanceListFilter(params).filter(instances);
        return new EntityInstanceListSorter(params).sort(instances);
    }

    private boolean matchesQueryIdentifier(final EntityInstance instance, final String identifier) {
        for (Field autoIncrementField : instance.getEntity().getFieldsOfType(FieldType.AUTO_INCREMENT)) {
            String idValue = instance.getFieldValue(autoIncrementField.getName()).asString();
            if (idValue.contentEquals(identifier)) {
                return true;
            }
            break;
        }

        String primaryKeyValue = instance.getPrimaryKeyValue();
        return primaryKeyValue != null && primaryKeyValue.contentEquals(identifier);
    }
}
