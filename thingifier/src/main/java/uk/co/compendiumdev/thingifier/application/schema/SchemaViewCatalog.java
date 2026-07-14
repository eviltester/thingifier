package uk.co.compendiumdev.thingifier.application.schema;

public interface SchemaViewCatalog {

    EntityTypeRef entityWithSingularOrPluralName(String term);

    boolean hasRelationshipNamed(String name);
}
