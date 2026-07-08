package uk.co.compendiumdev.thingifier.testsupport;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
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

    public Collection<EntityInstance> getInstances(final QueryFilterParams params) {
        return repository.listInstances(definition, params);
    }

    public EntityInstance findInstanceByPrimaryKey(final String primaryKeyValue) {
        return repository.findInstanceByPrimaryKey(definition, primaryKeyValue);
    }

    public EntityInstance findInstanceByFieldNameAndValue(
            final String fieldName, final String fieldValue) {
        return repository.findInstanceByFieldNameAndValue(definition, fieldName, fieldValue);
    }

    public List<EntityInstance> deleteInstance(final EntityInstance instance) {
        repository.deleteEntityInstance(instance);
        return List.of();
    }
}
