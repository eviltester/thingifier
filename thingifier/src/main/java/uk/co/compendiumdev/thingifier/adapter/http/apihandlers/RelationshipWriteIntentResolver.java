package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

final class RelationshipWriteIntentResolver {

    private final SchemaCatalog schema;

    RelationshipWriteIntentResolver(final SchemaCatalog schema) {
        this.schema = schema;
    }

    RelationshipWriteIntent intentFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final Set<RelationshipWriteOperation> allowedOperations) {
        if (verb == RoutingVerb.DELETE && route instanceof RelationshipInstanceRoute) {
            return RelationshipWriteIntent.of(RelationshipWriteOperation.DISCONNECT, List.of());
        }

        if (verb == RoutingVerb.POST && route instanceof RelationshipCollectionRoute) {
            return postIntentFor(
                    (RelationshipCollectionRoute) route, bodyFields, context, allowedOperations);
        }

        return RelationshipWriteIntent.none();
    }

    private RelationshipWriteIntent postIntentFor(
            final RelationshipCollectionRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final Set<RelationshipWriteOperation> allowedOperations) {
        List<NamedValue> childReferenceFields = childReferenceFields(route, bodyFields);
        if (childReferenceFields.isEmpty()) {
            return RelationshipWriteIntent.of(
                    RelationshipWriteOperation.CREATE_AND_CONNECT, childReferenceFields);
        }

        if (allowedOperations != null
                && allowedOperations.contains(RelationshipWriteOperation.UPDATE_CONNECTED)
                && referencesConnectedChild(route, childReferenceFields, context)) {
            return RelationshipWriteIntent.of(
                    RelationshipWriteOperation.UPDATE_CONNECTED, childReferenceFields);
        }

        return RelationshipWriteIntent.of(
                RelationshipWriteOperation.CONNECT_EXISTING, childReferenceFields);
    }

    private List<NamedValue> childReferenceFields(
            final RelationshipCollectionRoute route, final ApiBodyFields bodyFields) {
        EntityDefinition targetEntity = targetEntityFor(route);
        if (targetEntity == null) {
            return List.of();
        }

        List<NamedValue> references = new ArrayList<>();
        for (Map.Entry<String, String> entry : bodyFields.asFlattenedStringMap()) {
            Field field = targetEntity.getField(entry.getKey());
            if (field != null
                    && (field.getType() == FieldType.AUTO_GUID
                            || field.getType() == FieldType.AUTO_INCREMENT)) {
                references.add(new NamedValue(entry.getKey(), entry.getValue()));
            }
        }
        return references;
    }

    private boolean referencesConnectedChild(
            final RelationshipCollectionRoute route,
            final List<NamedValue> childReferenceFields,
            final ThingifierRequestContext context) {
        if (context == null) {
            return false;
        }

        EntityDefinition parentEntity =
                schema.definitionWithSingularOrPluralNamed(route.parentEntity().name());
        EntityDefinition targetEntity = targetEntityFor(route);
        if (parentEntity == null || targetEntity == null) {
            return false;
        }

        return context.hasRelatedEntityInstanceMatchingFields(
                parentEntity,
                route.parentIdentifier(),
                targetEntity,
                route.relationshipName(),
                childReferenceFields);
    }

    private EntityDefinition targetEntityFor(final RelationshipCollectionRoute route) {
        for (RelationshipSpec relationship : route.parentEntity().relationships()) {
            if (relationship.name().equals(route.relationshipName())) {
                return schema.definitionWithSingularOrPluralNamed(relationship.toEntityName());
            }
        }
        return null;
    }
}
