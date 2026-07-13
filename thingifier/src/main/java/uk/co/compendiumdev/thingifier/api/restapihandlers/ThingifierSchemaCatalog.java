package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

final class ThingifierSchemaCatalog implements SchemaCatalog {

    private final Thingifier thingifier;

    ThingifierSchemaCatalog(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    @Override
    public EntityDefinition definitionWithSingularOrPluralNamed(final String term) {
        return thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(term);
    }

    @Override
    public boolean hasRelationshipNamed(final String name) {
        return thingifier.getERmodel().getSchema().hasRelationshipNamed(name);
    }
}
