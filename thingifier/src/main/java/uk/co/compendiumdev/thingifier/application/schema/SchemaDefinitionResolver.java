package uk.co.compendiumdev.thingifier.application.schema;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public interface SchemaDefinitionResolver {

    EntityDefinition entityNamed(String name);

    EntityDefinition definitionWithSingularOrPluralName(String term);
}
