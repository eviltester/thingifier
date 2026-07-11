package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceRepository;

final class InMemoryEntityInstanceRepository implements EntityInstanceRepository {

    private final InMemoryThingStore store;

    InMemoryEntityInstanceRepository(final InMemoryThingStore store) {
        this.store = store;
    }

    @Override
    public EntityInstance create(final EntityInstanceDraft draft) {
        return store.createInstance(draft);
    }

    @Override
    public EntityInstance patch(final EntityInstance instance, final EntityInstanceDraft draft) {
        return store.patchInstance(instance, draft);
    }

    @Override
    public EntityInstance replace(final EntityInstance instance, final EntityInstanceDraft draft) {
        return store.replaceInstance(instance, draft);
    }

    @Override
    public void delete(final EntityInstance instance) {
        store.deleteEntityInstance(instance);
    }
}
