package uk.co.compendiumdev.thingifier.core.domain.datapopulator;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

public interface DataPopulator {

    /**
     * @deprecated Implement {@link RepositoryDataPopulator} so population writes
     * through the configured repository without hydrating a compatibility snapshot.
     */
    @Deprecated(forRemoval = true, since = "1.5.6")
    void populate(ERSchema schema, ERInstanceData database);
}
