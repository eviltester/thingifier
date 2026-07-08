package uk.co.compendiumdev.challenge.testsupport;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.Collection;
import java.util.List;

public final class RepositoryBackedTestCollection {

    private final EntityDefinition definition;
    private final ThingRepository repository;

    RepositoryBackedTestCollection(
            final EntityDefinition definition,
            final ThingRepository repository) {
        this.definition = definition;
        this.repository = repository;
    }

    public EntityDefinition definition() {
        return definition;
    }

    public EntityInstance addInstance(final EntityInstance instance) {
        return repository.addInstance(instance);
    }

    public int countInstances() {
        return repository.countInstances(definition);
    }

    public Collection<EntityInstance> getInstances() {
        return repository.listInstances(definition);
    }

    public EntityInstance findInstanceByPrimaryKey(final String primaryKeyValue) {
        return repository.findInstanceByPrimaryKey(definition, primaryKeyValue);
    }

    public List<EntityInstance> deleteInstance(final EntityInstance instance) {
        repository.deleteEntityInstance(instance);
        return List.of();
    }
}
