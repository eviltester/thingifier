package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipSnapshot {

    private final List<RelationshipLink> links;

    private RelationshipSnapshot(final List<RelationshipLink> links) {
        this.links = links;
    }

    static RelationshipSnapshot capture(final ThingStore store, final EntityInstance instance) {
        List<RelationshipLink> links = new ArrayList<>();
        Set<String> seenLinks = new HashSet<>();
        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships()) {
            for (EntityInstance related :
                    store.relationships().listRelated(instance, vector.getName())) {
                String key = vector.getName() + "|" + related.getInternalId();
                if (seenLinks.add(key)) {
                    links.add(
                            new RelationshipLink(
                                    vector.getName(),
                                    related,
                                    store.relationships().validate(related).isValid()));
                }
            }
        }
        return new RelationshipSnapshot(links);
    }

    void disconnectFrom(final ThingStore store, final EntityInstance instance) {
        for (RelationshipLink link : links) {
            store.relationships().disconnectBetween(instance, link.related, link.relationshipName);
        }
    }

    void deleteFormerDependentsMadeInvalidBy(
            final ThingStore store, final EntityInstance instance) {
        for (RelationshipLink link : links) {
            if (link.relatedWasValid
                    && !link.isStillRelatedTo(store, instance)
                    && !store.relationships().validate(link.related).isValid()) {
                store.entities().delete(link.related);
            }
        }
    }

    private static final class RelationshipLink {

        private final String relationshipName;
        private final EntityInstance related;
        private final boolean relatedWasValid;

        private RelationshipLink(
                final String relationshipName,
                final EntityInstance related,
                final boolean relatedWasValid) {
            this.relationshipName = relationshipName;
            this.related = related;
            this.relatedWasValid = relatedWasValid;
        }

        private boolean isStillRelatedTo(final ThingStore store, final EntityInstance instance) {
            for (EntityInstance current :
                    store.relationships().listRelated(instance, relationshipName)) {
                if (current.getInternalId().equals(related.getInternalId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
