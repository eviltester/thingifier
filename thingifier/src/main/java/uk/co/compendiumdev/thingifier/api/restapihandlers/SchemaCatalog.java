package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public interface SchemaCatalog {

    EntityDefinition definitionWithSingularOrPluralNamed(String term);

    boolean hasRelationshipNamed(String name);
}
