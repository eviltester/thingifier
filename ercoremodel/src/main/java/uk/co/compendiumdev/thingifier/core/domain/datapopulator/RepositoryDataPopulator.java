package uk.co.compendiumdev.thingifier.core.domain.datapopulator;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.repository.InMemoryThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public interface RepositoryDataPopulator extends DataPopulator {

    /**
     * @deprecated Compatibility bridge for older callers that still provide
     * ERInstanceData directly. Runtime population should call
     * {@link #populate(ERSchema, ThingRepository)}.
     */
    @Override
    @Deprecated
    default void populate(final ERSchema schema, final ERInstanceData database) {
        populate(schema, new InMemoryThingRepository("__repository-populator-compatibility", database));
    }

    void populate(ERSchema schema, ThingRepository repository);
}
