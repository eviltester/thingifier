package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public interface SchemaCatalog {

    EntityDefinition definitionWithSingularOrPluralNamed(String term);

    boolean hasRelationshipNamed(String name);
}
