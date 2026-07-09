package uk.co.compendiumdev.thingifier.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ERModelReport;
import uk.co.compendiumdev.thingifier.core.reporting.RepositoryJsonExporter;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public interface ThingRepository extends AutoCloseable {

    String databaseKey();

    void initializeFrom(ERSchema schema);

    void refreshSchema(ERSchema schema);

    default String exportDataAsJson(final ERSchema schema) {
        return new RepositoryJsonExporter(schema, this).asJson();
    }

    default String reportAsMarkdown(final ERSchema schema) {
        return new ERModelReport(schema, this).asMarkdown();
    }

    EntityInstance findEntityInstanceByGUID(String thingGUID);

    EntityInstance findInstanceByPrimaryKey(EntityDefinition entity, String primaryKeyValue);

    EntityInstance findInstanceByFieldNameAndValue(
            EntityDefinition entity, String fieldName, String fieldValue);

    Collection<EntityInstance> listInstances(EntityDefinition entity);

    List<EntityInstance> listInstances(EntityDefinition entity, QueryFilterParams queryParams);

    int countInstances(EntityDefinition entity);

    EntityInstance findInstanceByQueryIdentifier(EntityDefinition entity, String identifier);

    default List<EntityInstance> listRelatedInstances(
            EntityInstance instance, String relationshipName) {
        return listRelatedInstances(instance, relationshipName, new QueryFilterParams());
    }

    List<EntityInstance> listRelatedInstances(
            EntityInstance instance, String relationshipName, QueryFilterParams queryParams);

    EntityInstance createInstance(EntityInstanceDraft draft);

    EntityInstance patchInstance(EntityInstance instance, EntityInstanceDraft draft);

    EntityInstance replaceInstance(EntityInstance instance, EntityInstanceDraft draft);

    void deleteEntityInstance(EntityInstance instance);

    void clearAllData();

    void clearInstanceDataFor(String entityName);

    ValidationReport checkFieldsForUniqueNess(EntityInstance instance, boolean isAmendment);

    Map<String, AutoIncrement> countersFor(EntityDefinition entity);

    void resetAutoIncrementCounter(EntityDefinition entity, String fieldName);

    void setNextIdCountersToAccomodate(EntityDefinition entity, List<NamedValue> fieldValues);

    void connectRelationship(EntityInstance from, String relationshipName, EntityInstance to);

    List<EntityInstance> removeRelationshipsInvolving(
            EntityInstance parent, EntityInstance child, String relationshipName);

    List<EntityInstance> removeAllRelationships(EntityInstance instance);

    Collection<EntityInstance> getConnectedItems(EntityInstance instance, String relationshipName);

    @Override
    default void close() {
        // Most repository implementations do not own closeable resources.
    }
}
