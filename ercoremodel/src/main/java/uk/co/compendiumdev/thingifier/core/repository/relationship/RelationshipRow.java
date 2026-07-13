package uk.co.compendiumdev.thingifier.core.repository.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class RelationshipRow {

    private final RelationshipVectorDefinition vector;
    private final String fromInternalId;
    private final String toInternalId;

    public RelationshipRow(
            final RelationshipVectorDefinition vector,
            final String fromInternalId,
            final String toInternalId) {
        this.vector = vector;
        this.fromInternalId = fromInternalId;
        this.toInternalId = toInternalId;
    }

    public RelationshipVectorDefinition getVector() {
        return vector;
    }

    public String getFromInternalId() {
        return fromInternalId;
    }

    public String getToInternalId() {
        return toInternalId;
    }

    public boolean involves(final EntityInstance instance) {
        return fromInternalId.equals(instance.getInternalId())
                || toInternalId.equals(instance.getInternalId());
    }

    public boolean connects(final EntityInstance first, final EntityInstance second) {
        return (fromInternalId.equals(first.getInternalId())
                        && toInternalId.equals(second.getInternalId()))
                || (fromInternalId.equals(second.getInternalId())
                        && toInternalId.equals(first.getInternalId()));
    }

    public boolean relationshipDefinitionIsKnownAs(final String relationshipName) {
        return vector.getRelationshipDefinition().isKnownAs(relationshipName);
    }

    public boolean matchesEndpoint(
            final RelationshipVectorDefinition expectedVector,
            final RelationshipEndpoint endpoint,
            final String internalId) {
        if (vector != expectedVector) {
            return false;
        }
        if (endpoint == RelationshipEndpoint.FROM) {
            return fromInternalId.equals(internalId);
        }
        return toInternalId.equals(internalId);
    }
}
