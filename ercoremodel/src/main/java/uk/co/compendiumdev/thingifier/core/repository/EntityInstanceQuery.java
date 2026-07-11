package uk.co.compendiumdev.thingifier.core.repository;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public interface EntityInstanceQuery {

    EntityInstance findByGuid(String thingGUID);

    EntityInstance findByPrimaryKey(EntityDefinition entity, String primaryKeyValue);

    EntityInstance findByField(EntityDefinition entity, String fieldName, String fieldValue);

    List<EntityInstance> list(EntityDefinition entity);

    List<EntityInstance> list(EntityDefinition entity, QueryFilterParams queryParams);

    int count(EntityDefinition entity);

    EntityInstance findByQueryIdentifier(EntityDefinition entity, String identifier);
}
