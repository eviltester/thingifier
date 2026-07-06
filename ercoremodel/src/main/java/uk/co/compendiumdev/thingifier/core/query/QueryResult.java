package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

public interface QueryResult {

    boolean wasQueryIntendedToMatchAnInstance();

    boolean isResultACollection();

    List<EntityInstance> getListEntityInstances();

    EntityInstance getLastInstance();

    boolean lastMatchWasInstance();

    boolean lastMatchWasNothing();

    EntityDefinition resultContainsDefn();
}
