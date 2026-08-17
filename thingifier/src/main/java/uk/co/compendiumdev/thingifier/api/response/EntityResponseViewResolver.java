package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;

public interface EntityResponseViewResolver {

    EntityViewDefinition viewFor(EntityDefinition entity);
}
