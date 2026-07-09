package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceRepositoryAccess;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListFilter;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListSorter;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.validation.EntityInstanceWriteValidator;

public class InMemoryThingRepository implements ThingRepository {

    private final String databaseKey;
    private final InMemoryEntityInstanceStore instanceData;
    private final InMemoryRelationshipStore relationships;
    private final EntityInstanceWriteValidator writeValidator;

    public InMemoryThingRepository(final String databaseKey) {
        this(databaseKey, new InMemoryEntityInstanceStore());
    }

    InMemoryThingRepository(
            final String databaseKey, final InMemoryEntityInstanceStore instanceData) {
        this.databaseKey = databaseKey;
        this.instanceData = instanceData;
        this.relationships = new InMemoryRelationshipStore();
        this.writeValidator = new EntityInstanceWriteValidator(this);
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

    private InMemoryEntityInstanceCollection createInstanceCollectionFor(
            final EntityDefinition definition) {
        InMemoryEntityInstanceCollection existing =
                getInstanceCollectionForEntityNamed(definition.getName());
        if (existing != null) {
            return existing;
        }
        return instanceData.createInstanceCollectionFor(definition);
    }

    private void createInstanceCollectionsFrom(final ERSchema schema) {
        for (EntityDefinition defn : schema.getEntityDefinitions()) {
            createInstanceCollectionFor(defn);
        }
    }

    private InMemoryEntityInstanceCollection getInstanceCollectionForEntityNamed(
            final String entityName) {
        return instanceData.getInstanceCollectionForEntityNamed(entityName);
    }

    @Override
    public EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        return instanceData.findEntityInstanceByGUID(thingGUID);
    }

    @Override
    public EntityInstance findInstanceByPrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByPrimaryKey(primaryKeyValue);
    }

    @Override
    public EntityInstance findInstanceByFieldNameAndValue(
            final EntityDefinition entity, final String fieldName, final String fieldValue) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByFieldNameAndValue(fieldName, fieldValue);
    }

    @Override
    public Collection<EntityInstance> listInstances(final EntityDefinition entity) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
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
    public int countInstances(final EntityDefinition entity) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return 0;
        }
        return collection.countInstances();
    }

    @Override
    public EntityInstance findInstanceByQueryIdentifier(
            final EntityDefinition entity, final String identifier) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
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
    public EntityInstance createInstance(final EntityInstanceDraft draft) {
        EntityInstance instance = EntityInstance.fromDraft(draft);
        writeValidator.assertValidForCreate(instance);
        getInstanceCollectionForEntityNamed(instance.getEntity().getName()).addInstance(instance);
        return EntityInstanceRepositoryAccess.lock(instance);
    }

    @Override
    public EntityInstance patchInstance(
            final EntityInstance instance, final EntityInstanceDraft draft) {
        EntityInstance candidate = EntityInstanceRepositoryAccess.patch(instance, draft);
        writeValidator.assertValidForAmendment(candidate);
        return EntityInstanceRepositoryAccess.lock(
                EntityInstanceRepositoryAccess.apply(instance, draft));
    }

    @Override
    public EntityInstance replaceInstance(
            final EntityInstance instance, final EntityInstanceDraft draft) {
        EntityInstance candidate = EntityInstanceRepositoryAccess.replace(instance, draft);
        writeValidator.assertValidForAmendment(candidate);
        EntityInstanceRepositoryAccess.clearAllFields(instance);
        return EntityInstanceRepositoryAccess.lock(
                EntityInstanceRepositoryAccess.apply(instance, draft));
    }

    @Override
    public void deleteEntityInstance(final EntityInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Cannot delete a null entity instance");
        }
        deleteEntityInstanceAndMandatoryRelated(instance, new ArrayList<>());
    }

    private void deleteEntityInstanceAndMandatoryRelated(
            final EntityInstance instance, final List<String> alreadyDeleting) {
        if (alreadyDeleting.contains(instance.getInternalId())) {
            return;
        }

        alreadyDeleting.add(instance.getInternalId());
        List<EntityInstance> alsoDelete =
                relationships.removeAllRelationships(instance, this::findByInternalId);
        instanceData.deleteEntityInstance(instance);
        for (EntityInstance deleteMe : alsoDelete) {
            if (!deleteMe.getInternalId().equals(instance.getInternalId())) {
                deleteEntityInstanceAndMandatoryRelated(deleteMe, alreadyDeleting);
            }
        }
    }

    @Override
    public void clearAllData() {
        relationships.clear();
        instanceData.clearAllData();
    }

    @Override
    public void clearInstanceDataFor(final String entityName) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entityName);
        if (collection == null) {
            return;
        }
        for (EntityInstance instance : new ArrayList<>(collection.getInstances())) {
            deleteEntityInstance(instance);
        }
    }

    @Override
    public ValidationReport checkFieldsForUniqueNess(
            final EntityInstance instance, final boolean isAmendment) {
        return getInstanceCollectionForEntityNamed(instance.getEntity().getName())
                .checkFieldsForUniqueNess(instance, isAmendment);
    }

    @Override
    public Map<String, AutoIncrement> countersFor(final EntityDefinition entity) {
        return getInstanceCollectionForEntityNamed(entity.getName()).getCounters();
    }

    @Override
    public void resetAutoIncrementCounter(final EntityDefinition entity, final String fieldName) {
        Field field = entity.getField(fieldName);
        if (field == null || field.getType() != FieldType.AUTO_INCREMENT) {
            throw new IllegalArgumentException(
                    String.format(
                            "%s is not an auto-increment field on %s",
                            fieldName, entity.getName()));
        }

        AutoIncrement counter = countersFor(entity).get(fieldName);
        if (counter != null) {
            counter.incrementToNextAbove(field.getDefaultValue().asInteger() - 1);
        }
    }

    @Override
    public void setNextIdCountersToAccomodate(
            final EntityDefinition entity, final List<NamedValue> fieldValues) {
        for (NamedValue fieldNameValue : fieldValues) {
            Field field = entity.getField(fieldNameValue.getName());
            if (field != null && field.getType() == FieldType.AUTO_INCREMENT) {
                AutoIncrement counter = countersFor(entity).get(field.getName());
                if (counter != null) {
                    counter.incrementToNextAbove(Integer.parseInt(fieldNameValue.value));
                }
            }
        }
    }

    @Override
    public void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        relationships.connect(from, relationshipName, to, this::findByInternalId);
    }

    @Override
    public List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        return relationships.removeRelationshipsInvolving(
                parent, child, relationshipName, this::findByInternalId);
    }

    @Override
    public List<EntityInstance> removeAllRelationships(final EntityInstance instance) {
        return relationships.removeAllRelationships(instance, this::findByInternalId);
    }

    @Override
    public List<EntityInstance> listRelatedInstances(
            final EntityInstance instance,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;
        List<EntityInstance> instances =
                new ArrayList<>(
                        relationships.listRelatedInstances(
                                instance, relationshipName, this::findByInternalId));
        instances = new EntityInstanceListFilter(params).filter(instances);
        return new EntityInstanceListSorter(params).sort(instances);
    }

    @Override
    public boolean hasRelationshipInstances(final EntityInstance instance) {
        return relationships.hasRelationshipInstances(instance);
    }

    @Override
    public ValidationReport validateRelationships(final EntityInstance instance) {
        return relationships.validateRelationships(instance, this::findByInternalId);
    }

    private boolean matchesQueryIdentifier(final EntityInstance instance, final String identifier) {
        for (Field autoIncrementField :
                instance.getEntity().getFieldsOfType(FieldType.AUTO_INCREMENT)) {
            String idValue = instance.getFieldValue(autoIncrementField.getName()).asString();
            if (idValue.contentEquals(identifier)) {
                return true;
            }
            break;
        }

        String primaryKeyValue = instance.getPrimaryKeyValue();
        return primaryKeyValue != null && primaryKeyValue.contentEquals(identifier);
    }

    private EntityInstance findByInternalId(
            final EntityDefinition entity, final String internalId) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByInternalID(internalId);
    }
}
