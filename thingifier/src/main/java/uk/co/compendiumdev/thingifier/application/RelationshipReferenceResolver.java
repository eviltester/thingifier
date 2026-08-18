package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipReferenceResolver {

    private final ThingStore store;
    private final SchemaDefinitionResolver schema;

    RelationshipReferenceResolver(final ThingStore store, final SchemaDefinitionResolver schema) {
        this.store = store;
        this.schema = schema;
    }

    Resolution resolve(
            final EntityInstance instance, final List<RelationshipReference> references) {
        return resolve(instance.getEntity(), references);
    }

    Resolution resolve(
            final EntityDefinition sourceEntity, final List<RelationshipReference> references) {
        List<RelationshipConnection> relationships = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (RelationshipReference reference : references) {
            String configurationError = fieldBindingConfigurationError(sourceEntity, reference);
            if (configurationError != null) {
                errors.add(configurationError);
                continue;
            }

            EntityInstance related = resolveRelationshipReference(sourceEntity, reference);
            if (related == null) {
                errors.add(missingRelationshipReferenceMessage(reference));
            } else {
                relationships.add(
                        new RelationshipConnection(reference.relationshipName(), related));
            }
        }

        return new Resolution(relationships, errors);
    }

    private EntityInstance resolveRelationshipReference(
            final EntityDefinition sourceEntity, final RelationshipReference reference) {
        if (reference.hasExplicitTargetEntity()) {
            return resolveExplicitRelationshipReference(reference);
        }

        for (RelationshipVectorDefinition vector :
                sourceEntity.related().getRelationships(reference.relationshipName())) {
            EntityDefinition relatedEntity = vector.getTo();
            EntityInstance related =
                    store.entityQueries()
                            .findByField(
                                    relatedEntity,
                                    reference.referenceFieldName(),
                                    reference.referenceValue());
            if (related == null) {
                related =
                        store.entityQueries()
                                .findByQueryIdentifier(relatedEntity, reference.referenceValue());
            }
            if (related != null) {
                return related;
            }
        }

        return null;
    }

    private String fieldBindingConfigurationError(
            final EntityDefinition sourceEntity, final RelationshipReference reference) {
        if (!reference.isFieldBinding()) {
            return null;
        }

        EntityDefinition target = schema.entityNamed(reference.targetEntityName());
        if (target == null) {
            return "Reference target entity not found " + reference.targetEntityName();
        }

        Field targetField = target.getField(reference.referenceFieldName());
        if (targetField == null) {
            return String.format(
                    "Reference target field not found %s.%s",
                    target.getName(), reference.referenceFieldName());
        }

        if (!canUniquelyResolve(target, targetField)) {
            return String.format(
                    "Reference target field %s.%s must be unique, primary, or protected",
                    target.getName(), targetField.getName());
        }

        RelationshipVectorDefinition relationship =
                sourceEntity.getNamedRelationshipTo(reference.relationshipName(), target);
        if (relationship == null) {
            return String.format(
                    "Relationship %s does not connect %s to %s",
                    reference.relationshipName(), sourceEntity.getName(), target.getName());
        }
        return null;
    }

    private boolean canUniquelyResolve(final EntityDefinition target, final Field targetField) {
        if (targetField.mustBeUnique()) {
            return true;
        }
        if (target.hasPrimaryKeyField()
                && target.getPrimaryKeyField().getName().equals(targetField.getName())) {
            return true;
        }
        return targetField.getType() == FieldType.AUTO_INCREMENT
                || targetField.getType() == FieldType.AUTO_GUID;
    }

    private EntityInstance resolveExplicitRelationshipReference(
            final RelationshipReference reference) {
        EntityDefinition target = schema.entityNamed(reference.targetEntityName());
        EntityInstance related =
                store.entityQueries().findByQueryIdentifier(target, reference.referenceValue());
        if (related == null) {
            related =
                    store.entityQueries()
                            .findByField(
                                    target,
                                    reference.referenceFieldName(),
                                    reference.referenceValue());
        }
        return related;
    }

    private String missingRelationshipReferenceMessage(final RelationshipReference reference) {
        if (reference.hasExplicitTargetEntity()) {
            return String.format(
                    "cannot find %s of %s to relate to with %s %s",
                    reference.referenceFieldName(),
                    reference.targetTerm(),
                    reference.referenceFieldName(),
                    reference.referenceValue());
        }

        return String.format(
                "cannot find %s to relate to with %s %s",
                reference.relationshipName(),
                reference.referenceFieldName(),
                reference.referenceValue());
    }

    static final class Resolution {

        private final List<RelationshipConnection> relationships;
        private final List<String> errors;

        private Resolution(
                final List<RelationshipConnection> relationships, final List<String> errors) {
            this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
            this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        List<RelationshipConnection> relationships() {
            return relationships;
        }

        List<String> errors() {
            return errors;
        }
    }
}
