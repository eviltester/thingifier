package uk.co.compendiumdev.thingifier.core.repository.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

@FunctionalInterface
public interface RelationshipRowLookup {

    boolean exists(RelationshipVectorDefinition vector, String fromInternalId, String toInternalId);
}
