package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.ArrayList;
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
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListFilter;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListSorter;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceRepository;
import uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;
import uk.co.compendiumdev.thingifier.core.repository.RepositoryAdministration;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreTransaction;
import uk.co.compendiumdev.thingifier.core.repository.validation.EntityInstanceWriteValidator;

public class InMemoryThingStore implements ThingStore {

    private final String databaseKey;
    private final InMemoryEntityInstanceStore instanceData;
    private final InMemoryRelationshipStore relationships;
    private final EntityInstanceWriteValidator writeValidator;
    private final EntityInstanceRepository entityRepository;
    private final EntityInstanceQuery entityQuery;
    private final RelationshipRepository relationshipRepository;
    private final RepositoryAdministration administration;

    public InMemoryThingStore(final String databaseKey) {
        this(databaseKey, new InMemoryEntityInstanceStore());
    }

    InMemoryThingStore(final String databaseKey, final InMemoryEntityInstanceStore instanceData) {
        this.databaseKey = databaseKey;
        this.instanceData = instanceData;
        this.relationships = new InMemoryRelationshipStore();
        this.writeValidator = new EntityInstanceWriteValidator(this::checkFieldsForUniqueNess);
        this.entityRepository = new InMemoryEntityInstanceRepository(this);
        this.entityQuery = new InMemoryEntityInstanceQuery(this);
        this.relationshipRepository = new InMemoryRelationshipRepository(this);
        this.administration = new InMemoryRepositoryAdministration(this);
    }

    @Override
    public String databaseKey() {
        return databaseKey;
    }

    @Override
    public EntityInstanceRepository entities() {
        return entityRepository;
    }

    @Override
    public EntityInstanceQuery entityQueries() {
        return entityQuery;
    }

    @Override
    public RelationshipRepository relationships() {
        return relationshipRepository;
    }

    @Override
    public RepositoryAdministration administration() {
        return administration;
    }

    @Override
    public ThingStoreTransaction beginTransaction() {
        return new InMemoryThingStoreTransaction(instanceData.snapshot(), relationships.snapshot());
    }

    void initializeFrom(final ERSchema schema) {
        refreshSchema(schema);
    }

    void refreshSchema(final ERSchema schema) {
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

    EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        return instanceData.findEntityInstanceByGUID(thingGUID);
    }

    EntityInstance findInstanceByPrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByPrimaryKey(primaryKeyValue);
    }

    EntityInstance findInstanceByFieldNameAndValue(
            final EntityDefinition entity, final String fieldName, final String fieldValue) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return null;
        }
        return collection.findInstanceByFieldNameAndValue(fieldName, fieldValue);
    }

    List<EntityInstance> listInstances(final EntityDefinition entity) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return List.of();
        }
        return new ArrayList<>(collection.getInstances());
    }

    List<EntityInstance> listInstances(
            final EntityDefinition entity, final QueryFilterParams queryParams) {
        List<EntityInstance> instances = new ArrayList<>(listInstances(entity));
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;
        instances = new EntityInstanceListFilter(params).filter(instances);
        return new EntityInstanceListSorter(params).sort(instances);
    }

    int countInstances(final EntityDefinition entity) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entity.getName());
        if (collection == null) {
            return 0;
        }
        return collection.countInstances();
    }

    EntityInstance findInstanceByQueryIdentifier(
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

    EntityInstance createInstance(final EntityInstanceDraft draft) {
        MutableEntityInstance mutableInstance = MutableEntityInstance.fromDraft(draft);
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(mutableInstance.getEntity().getName());
        EntityInstance instance = collection.prepareInstanceForInsert(mutableInstance);
        writeValidator.assertValidForCreate(instance);
        return collection.addInstance(instance);
    }

    EntityInstance patchInstance(final EntityInstance instance, final EntityInstanceDraft draft) {
        EntityInstance candidate =
                MutableEntityInstance.fromExisting(instance).patch(draft).toEntityInstance();
        writeValidator.assertValidForAmendment(candidate);
        return getInstanceCollectionForEntityNamed(instance.getEntity().getName())
                .replaceInstance(candidate);
    }

    EntityInstance replaceInstance(final EntityInstance instance, final EntityInstanceDraft draft) {
        EntityInstance candidate =
                MutableEntityInstance.fromExisting(instance).replace(draft).toEntityInstance();
        writeValidator.assertValidForAmendment(candidate);
        return getInstanceCollectionForEntityNamed(instance.getEntity().getName())
                .replaceInstance(candidate);
    }

    void deleteEntityInstance(final EntityInstance instance) {
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

    void clearAllData() {
        relationships.clear();
        instanceData.clearAllData();
    }

    void clearInstanceDataFor(final String entityName) {
        InMemoryEntityInstanceCollection collection =
                getInstanceCollectionForEntityNamed(entityName);
        if (collection == null) {
            return;
        }
        for (EntityInstance instance : new ArrayList<>(collection.getInstances())) {
            deleteEntityInstance(instance);
        }
    }

    ValidationReport checkFieldsForUniqueNess(
            final EntityInstance instance, final boolean isAmendment) {
        return getInstanceCollectionForEntityNamed(instance.getEntity().getName())
                .checkFieldsForUniqueNess(instance, isAmendment);
    }

    Map<String, AutoIncrement> countersFor(final EntityDefinition entity) {
        return getInstanceCollectionForEntityNamed(entity.getName()).getCounters();
    }

    void resetAutoIncrementCounter(final EntityDefinition entity, final String fieldName) {
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

    boolean resetAutoIncrementCounterWhenNextValueAbove(
            final EntityDefinition entity, final String fieldName, final int ceiling) {
        AutoIncrement counter = countersFor(entity).get(fieldName);
        if (counter != null && counter.peekNextValue() > ceiling) {
            resetAutoIncrementCounter(entity, fieldName);
            return true;
        }
        return false;
    }

    void setNextIdCountersToAccomodate(
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

    void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        relationships.connect(from, relationshipName, to, this::findByInternalId);
    }

    void removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        List<EntityInstance> alsoDelete =
                relationships.removeRelationshipsInvolving(
                        parent, child, relationshipName, this::findByInternalId);
        deleteMandatoryDependents(alsoDelete);
    }

    void disconnectRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        relationships.removeRelationshipsInvolving(
                parent, child, relationshipName, this::findByInternalId);
    }

    void removeAllRelationships(final EntityInstance instance) {
        List<EntityInstance> alsoDelete =
                relationships.removeAllRelationships(instance, this::findByInternalId);
        deleteMandatoryDependents(alsoDelete);
    }

    private void deleteMandatoryDependents(final List<EntityInstance> alsoDelete) {
        List<String> alreadyDeleting = new ArrayList<>();
        for (EntityInstance deleteMe : alsoDelete) {
            deleteEntityInstanceAndMandatoryRelated(deleteMe, alreadyDeleting);
        }
    }

    List<EntityInstance> listRelatedInstances(
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

    boolean hasRelationshipInstances(final EntityInstance instance) {
        return relationships.hasRelationshipInstances(instance);
    }

    ValidationReport validateRelationships(final EntityInstance instance) {
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

    private final class InMemoryThingStoreTransaction implements ThingStoreTransaction {

        private final InMemoryEntityInstanceStore.Snapshot instanceSnapshot;
        private final InMemoryRelationshipStore.Snapshot relationshipSnapshot;
        private boolean completed;

        private InMemoryThingStoreTransaction(
                final InMemoryEntityInstanceStore.Snapshot instanceSnapshot,
                final InMemoryRelationshipStore.Snapshot relationshipSnapshot) {
            this.instanceSnapshot = instanceSnapshot;
            this.relationshipSnapshot = relationshipSnapshot;
        }

        @Override
        public void commit() {
            completed = true;
        }

        @Override
        public void rollback() {
            if (completed) {
                return;
            }
            relationships.restore(relationshipSnapshot);
            instanceData.restore(instanceSnapshot);
            completed = true;
        }

        @Override
        public void close() {
            if (!completed) {
                rollback();
            }
        }
    }
}
