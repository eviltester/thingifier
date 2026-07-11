package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public interface EntityInstanceRepository {

    EntityInstance create(EntityInstanceDraft draft);

    EntityInstance patch(EntityInstance instance, EntityInstanceDraft draft);

    EntityInstance replace(EntityInstance instance, EntityInstanceDraft draft);

    void delete(EntityInstance instance);
}
