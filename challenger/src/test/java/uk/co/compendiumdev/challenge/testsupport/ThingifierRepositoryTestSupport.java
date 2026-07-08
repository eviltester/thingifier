package uk.co.compendiumdev.challenge.testsupport;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public final class ThingifierRepositoryTestSupport {

    private ThingifierRepositoryTestSupport() {
    }

    public static ThingRepository repository(
            final Thingifier thingifier,
            final String databaseName) {
        return thingifier.getRepository(databaseName);
    }

    public static EntityDefinition entity(
            final Thingifier thingifier,
            final String databaseName,
            final String entityName) {
        return thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(entityName);
    }
}
