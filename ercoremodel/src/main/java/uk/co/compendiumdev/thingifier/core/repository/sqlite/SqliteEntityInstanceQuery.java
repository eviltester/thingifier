package uk.co.compendiumdev.thingifier.core.repository.sqlite;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

final class SqliteEntityInstanceQuery implements EntityInstanceQuery {

    private final SqliteThingStore store;

    SqliteEntityInstanceQuery(final SqliteThingStore store) {
        this.store = store;
    }

    @Override
    public EntityInstance findByGuid(final String thingGUID) {
        return store.findEntityInstanceByGUID(thingGUID);
    }

    @Override
    public EntityInstance findByPrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        return store.findInstanceByPrimaryKey(entity, primaryKeyValue);
    }

    @Override
    public EntityInstance findByField(
            final EntityDefinition entity, final String fieldName, final String fieldValue) {
        return store.findInstanceByFieldNameAndValue(entity, fieldName, fieldValue);
    }

    @Override
    public List<EntityInstance> list(final EntityDefinition entity) {
        return store.listInstances(entity);
    }

    @Override
    public List<EntityInstance> list(
            final EntityDefinition entity, final QueryFilterParams queryParams) {
        return store.listInstances(entity, queryParams);
    }

    @Override
    public int count(final EntityDefinition entity) {
        return store.countInstances(entity);
    }

    @Override
    public EntityInstance findByQueryIdentifier(
            final EntityDefinition entity, final String identifier) {
        return store.findInstanceByQueryIdentifier(entity, identifier);
    }
}
