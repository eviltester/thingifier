package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
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
        List<RelationshipConnection> relationships = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (RelationshipReference reference : references) {
            EntityInstance related = resolveRelationshipReference(instance, reference);
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
            final EntityInstance instance, final RelationshipReference reference) {
        if (reference.hasExplicitTargetEntity()) {
            return resolveExplicitRelationshipReference(reference);
        }

        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships(reference.relationshipName())) {
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
