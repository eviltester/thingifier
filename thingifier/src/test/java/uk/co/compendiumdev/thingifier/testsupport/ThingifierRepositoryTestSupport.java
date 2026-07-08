package uk.co.compendiumdev.thingifier.testsupport;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public final class ThingifierRepositoryTestSupport {

    private ThingifierRepositoryTestSupport() {
    }

    public static RepositoryBackedTestCollection collection(
            final Thingifier thingifier,
            final String entityName) {
        return collection(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, entityName);
    }

    public static RepositoryBackedTestCollection collection(
            final Thingifier thingifier,
            final String databaseName,
            final String entityName) {
        EntityDefinition definition =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(entityName);
        ThingRepository repository = thingifier.getRepository(databaseName);
        return new RepositoryBackedTestCollection(definition, repository);
    }
}
