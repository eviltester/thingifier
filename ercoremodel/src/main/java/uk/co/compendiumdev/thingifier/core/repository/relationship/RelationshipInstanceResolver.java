package uk.co.compendiumdev.thingifier.core.repository.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

@FunctionalInterface
public interface RelationshipInstanceResolver {

    EntityInstance resolve(EntityDefinition entity, String internalId);
}
