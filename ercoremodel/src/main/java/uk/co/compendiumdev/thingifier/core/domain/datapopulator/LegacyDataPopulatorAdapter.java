package uk.co.compendiumdev.thingifier.core.domain.datapopulator;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public final class LegacyDataPopulatorAdapter {

    private LegacyDataPopulatorAdapter() {
    }

    @Deprecated(forRemoval = true, since = "1.5.6")
    public static void populate(
            final DataPopulator dataPopulator,
            final ERSchema schema,
            final ThingRepository repository) {
        dataPopulator.populate(schema, repository.getInstanceData());
        repository.flush();
    }
}
