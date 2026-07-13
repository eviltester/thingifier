package uk.co.compendiumdev.thingifier.core.repository.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

@FunctionalInterface
public interface RelationshipEndpointCounter {

    int count(
            RelationshipVectorDefinition vector, RelationshipEndpoint endpoint, String internalId);
}
