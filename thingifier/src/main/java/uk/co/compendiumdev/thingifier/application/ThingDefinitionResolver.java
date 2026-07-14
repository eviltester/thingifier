package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ThingDefinitionResolver {

    private final ThingStore store;
    private final SchemaDefinitionResolver schema;

    ThingDefinitionResolver(final ThingStore store, final SchemaDefinitionResolver schema) {
        this.store = store;
        this.schema = schema;
    }

    EntityDefinition entityNamed(final String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            return null;
        }
        EntityDefinition entity = schema.entityNamed(entityName);
        if (entity != null) {
            return entity;
        }
        return schema.definitionWithSingularOrPluralName(entityName);
    }

    EntityInstance resolveInstance(final EntityDefinition entity, final String queryIdentifier) {
        if (entity == null) {
            return null;
        }
        return store.entityQueries().findByQueryIdentifier(entity, queryIdentifier);
    }
}
