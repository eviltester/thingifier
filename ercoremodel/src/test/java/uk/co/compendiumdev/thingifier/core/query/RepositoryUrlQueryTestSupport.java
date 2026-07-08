package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

final class RepositoryUrlQueryTestSupport {

    private RepositoryUrlQueryTestSupport() {
    }

    static RepositoryUrlQuery query(final EntityRelModel model, final String query) {
        return new RepositoryUrlQuery(
                model.getSchema(),
                model.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                query);
    }

    static EntityInstance add(
            final EntityRelModel model,
            final String entityName,
            final String fieldName,
            final String value) {
        EntityDefinition definition = model.getSchema().getEntityDefinitionNamed(entityName);
        EntityInstance instance = new EntityInstance(definition).setValue(fieldName, value);
        return model.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).addInstance(instance);
    }
}
