package uk.co.compendiumdev.thingifier.core.repository;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public interface RepositoryAdministration {

    void initializeFrom(ERSchema schema);

    void refreshSchema(ERSchema schema);

    void clearAllData();

    void clearEntityData(String entityName);

    void resetAutoIncrementCounter(EntityDefinition entity, String fieldName);

    boolean resetAutoIncrementCounterWhenNextValueAbove(
            EntityDefinition entity, String fieldName, int ceiling);

    void accommodateProtectedIds(EntityDefinition entity, List<NamedValue> fieldValues);
}
