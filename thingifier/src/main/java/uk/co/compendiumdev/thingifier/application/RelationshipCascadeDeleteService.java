package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipCascadeDeleteService {

    private final ThingStore store;

    RelationshipCascadeDeleteService(final ThingStore store) {
        this.store = store;
    }

    void deleteInstance(final EntityInstance instance) {
        deleteInstance(instance, new HashSet<>());
    }

    void deleteTargetAfterDisconnect(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstance child) {
        RelationshipVectorDefinition vector =
                parent.getEntity().getNamedRelationshipTo(relationshipName, child.getEntity());
        if (vector != null && vector.shouldDeleteTargetWhenDisconnected()) {
            deleteInstance(child);
        }
    }

    private void deleteInstance(final EntityInstance instance, final Set<String> alreadyDeleting) {
        if (instance == null || !alreadyDeleting.add(instance.getInternalId())) {
            return;
        }
        if (!isPersisted(instance)) {
            return;
        }

        for (EntityInstance target : cascadeTargetsFor(instance)) {
            deleteInstance(target, alreadyDeleting);
        }

        if (isPersisted(instance)) {
            store.entities().delete(instance);
        }
    }

    private List<EntityInstance> cascadeTargetsFor(final EntityInstance instance) {
        List<EntityInstance> targets = new ArrayList<>();
        for (RelationshipVectorDefinition relationship :
                instance.getEntity().related().getRelationships()) {
            if (relationship.shouldDeleteTargetsWhenSourceDeleted()) {
                targets.addAll(store.relationships().listRelated(instance, relationship.getName()));
            }
        }
        return targets;
    }

    private boolean isPersisted(final EntityInstance instance) {
        if (instance.getPrimaryKeyValue() != null) {
            EntityInstance found =
                    store.entityQueries()
                            .findByQueryIdentifier(
                                    instance.getEntity(), instance.getPrimaryKeyValue());
            return found != null && found.getInternalId().equals(instance.getInternalId());
        }

        for (EntityInstance candidate : store.entityQueries().list(instance.getEntity())) {
            if (candidate.getInternalId().equals(instance.getInternalId())) {
                return true;
            }
        }
        return false;
    }
}
