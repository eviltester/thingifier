package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.FieldReferenceSpec;
import uk.co.compendiumdev.thingifier.application.schema.FieldSpec;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

public final class ThingifierSchemaCatalog implements SchemaCatalog {

    private final Thingifier thingifier;

    public ThingifierSchemaCatalog(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    @Override
    public EntityDefinition definitionWithSingularOrPluralNamed(final String term) {
        return thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed(term);
    }

    @Override
    public EntityDefinition entityNamed(final String name) {
        return thingifier.getDefinitionNamed(name);
    }

    @Override
    public EntityTypeRef entityWithSingularOrPluralName(final String term) {
        EntityDefinition definition = definitionWithSingularOrPluralNamed(term);
        if (definition == null) {
            return null;
        }
        return refFor(definition);
    }

    @Override
    public boolean hasRelationshipNamed(final String name) {
        return thingifier.getERmodel().getSchema().hasRelationshipNamed(name);
    }

    private EntityTypeRef refFor(final EntityDefinition definition) {
        String primaryKey =
                definition.hasPrimaryKeyField() ? definition.getPrimaryKeyField().getName() : "";
        return new EntityTypeRef(
                definition.getName(),
                definition.getPlural(),
                primaryKey,
                fieldsFor(definition),
                relationshipsFor(definition));
    }

    private List<FieldSpec> fieldsFor(final EntityDefinition definition) {
        List<FieldSpec> fields = new ArrayList<>();
        for (String fieldName : definition.getFieldNames()) {
            Field field = definition.getField(fieldName);
            FieldReferenceSpec reference = null;
            if (field.hasRelationshipReference()) {
                reference =
                        new FieldReferenceSpec(
                                field.relationshipReference().targetEntity().getName(),
                                field.relationshipReference().targetFieldName(),
                                field.relationshipReference().relationshipName());
            }
            fields.add(
                    new FieldSpec(
                            field.getName(), field.getType(), field.mustBeUnique(), reference));
        }
        return fields;
    }

    private List<RelationshipSpec> relationshipsFor(final EntityDefinition definition) {
        List<RelationshipSpec> relationships = new ArrayList<>();
        for (RelationshipVectorDefinition relationship : definition.related().getRelationships()) {
            relationships.add(
                    new RelationshipSpec(
                            relationship.getName(),
                            relationship.getFrom().getName(),
                            relationship.getTo().getName()));
        }
        return relationships;
    }
}
