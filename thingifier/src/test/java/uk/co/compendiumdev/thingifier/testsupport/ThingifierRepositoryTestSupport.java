package uk.co.compendiumdev.thingifier.testsupport;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public final class ThingifierRepositoryTestSupport {

    private ThingifierRepositoryTestSupport() {
    }

    public static ThingRepository repository(final Thingifier thingifier) {
        return repository(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    public static ThingRepository repository(
            final Thingifier thingifier,
            final String databaseName) {
        return thingifier.getRepository(databaseName);
    }

    public static EntityDefinition entity(
            final Thingifier thingifier,
            final String entityName) {
        return entity(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, entityName);
    }

    public static EntityDefinition entity(
            final Thingifier thingifier,
            final String databaseName,
            final String entityName) {
        return thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(entityName);
    }
}
