package uk.co.compendiumdev.thingifier.core.domain.datapopulator;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public interface RepositoryDataPopulator extends DataPopulator {

    void populate(ERSchema schema, ThingRepository repository);
}
