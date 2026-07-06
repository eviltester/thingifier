package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ThingRepository extends AutoCloseable {

    String databaseKey();

    void initializeFrom(ERSchema schema);

    void refreshSchema(ERSchema schema);

    @Deprecated
    ERInstanceData getInstanceData();

    EntityInstanceCollection createInstanceCollectionFor(EntityDefinition definition);

    void createInstanceCollectionsFrom(ERSchema schema);

    List<EntityInstanceCollection> getAllInstanceCollections();

    @Deprecated
    EntityInstanceCollection getInstanceCollectionForEntityNamed(String entityName);

    EntityInstance findEntityInstanceByGUID(String thingGUID);

    EntityInstance findInstanceByPrimaryKey(EntityDefinition entity, String primaryKeyValue);

    EntityInstance findInstanceByFieldNameAndValue(EntityDefinition entity, String fieldName, String fieldValue);

    Collection<EntityInstance> listInstances(EntityDefinition entity);

    List<EntityInstance> listInstances(EntityDefinition entity, QueryFilterParams queryParams);

    EntityInstance findInstanceByQueryIdentifier(EntityDefinition entity, String identifier);

    default List<EntityInstance> listRelatedInstances(
            EntityInstance instance, String relationshipName) {
        return listRelatedInstances(instance, relationshipName, new QueryFilterParams());
    }

    List<EntityInstance> listRelatedInstances(
            EntityInstance instance, String relationshipName, QueryFilterParams queryParams);

    EntityInstance addInstance(EntityInstance instance);

    EntityInstance updateInstance(EntityInstance instance);

    void deleteEntityInstance(EntityInstance instance);

    void clearAllData();

    void clearInstanceDataFor(String entityName);

    ValidationReport checkFieldsForUniqueNess(EntityInstance instance, boolean isAmendment);

    Map<String, AutoIncrement> countersFor(EntityDefinition entity);

    void setNextIdCountersToAccomodate(EntityDefinition entity, List<NamedValue> fieldValues);

    void connectRelationship(EntityInstance from, String relationshipName, EntityInstance to);

    List<EntityInstance> removeRelationshipsInvolving(
            EntityInstance parent, EntityInstance child, String relationshipName);

    List<EntityInstance> removeAllRelationships(EntityInstance instance);

    Collection<EntityInstance> getConnectedItems(EntityInstance instance, String relationshipName);

    default boolean hasLoadedCompatibilitySnapshot() {
        return true;
    }

    default void flush() {
        // Allows persistent repositories to sync legacy direct collection mutations.
    }

    @Override
    default void close() {
        // Most repository implementations do not own closeable resources.
    }
}
