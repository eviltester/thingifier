package uk.co.compendiumdev.thingifier.core.domain.datapopulator;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

public interface DataPopulator {

    void populate(ERSchema schema, ERInstanceData database);
}
