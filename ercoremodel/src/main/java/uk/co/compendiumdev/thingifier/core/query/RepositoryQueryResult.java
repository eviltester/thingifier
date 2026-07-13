package uk.co.compendiumdev.thingifier.core.query;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public interface RepositoryQueryResult {

    boolean wasQueryIntendedToMatchAnInstance();

    boolean isResultACollection();

    List<EntityInstance> getListEntityInstances();

    EntityInstance getLastInstance();

    boolean lastMatchWasInstance();

    boolean lastMatchWasNothing();

    EntityDefinition resultContainsDefn();
}
