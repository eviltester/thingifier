package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.application.schema.SchemaViewCatalog;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public interface SchemaCatalog extends SchemaViewCatalog, SchemaDefinitionResolver {

    EntityDefinition definitionWithSingularOrPluralNamed(String term);

    @Override
    default EntityDefinition definitionWithSingularOrPluralName(final String term) {
        return definitionWithSingularOrPluralNamed(term);
    }

    @Override
    boolean hasRelationshipNamed(String name);
}
