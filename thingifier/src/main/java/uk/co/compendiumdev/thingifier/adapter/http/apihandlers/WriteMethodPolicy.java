package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class WriteMethodPolicy {

    private final ThingifierApiRuntime runtime;

    public WriteMethodPolicy(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    public ApiResponse rejectIfNotAllowed(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (route instanceof CollectionRoute || route instanceof InstanceRoute) {
            return rejectEntityWriteIfNotAllowed(verb, route, context);
        }

        if (route instanceof RelationshipCollectionRoute
                || route instanceof RelationshipInstanceRoute) {
            return rejectRelationshipWriteIfNotAllowed(verb, route, bodyFields);
        }

        return null;
    }

    private ApiResponse rejectEntityWriteIfNotAllowed(
            final RoutingVerb verb,
            final ThingRoute route,
            final ThingifierRequestContext context) {
        if (verb == RoutingVerb.PATCH) {
            return rejectEntityPatchIfNotAllowed(route, context);
        }

        EntityWriteOperation operation = entityOperationFor(verb, route, context);
        if (operation == null) {
            return null;
        }

        Set<EntityWriteOperation> allowed = entityOperationsFor(verb, route);
        if (allowed.contains(operation)) {
            return null;
        }

        return methodNotAllowed(allowHeaderFor(route, context, verb, operation));
    }

    private ApiResponse rejectEntityPatchIfNotAllowed(
            final ThingRoute route, final ThingifierRequestContext context) {
        if (route instanceof CollectionRoute) {
            return methodNotAllowed(
                    allowHeaderFor(route, context, RoutingVerb.PATCH, EntityWriteOperation.UPDATE));
        }

        if (route instanceof InstanceRoute && entityPatchUpdateStylesFor(route).isEmpty()) {
            return methodNotAllowed(
                    allowHeaderFor(route, context, RoutingVerb.PATCH, EntityWriteOperation.UPDATE));
        }

        return null;
    }

    private ApiResponse rejectRelationshipWriteIfNotAllowed(
            final RoutingVerb verb, final ThingRoute route, final ApiBodyFields bodyFields) {
        RelationshipWriteOperation operation = relationshipOperationFor(verb, route, bodyFields);
        if (operation == null) {
            return null;
        }

        Set<RelationshipWriteOperation> allowed = relationshipOperationsFor(verb, route);
        if (allowed.contains(operation)) {
            return null;
        }

        return methodNotAllowed(allowHeaderFor(route, verb, operation));
    }

    private ApiResponse methodNotAllowed(final String allowHeader) {
        return ApiResponse.error(405, "Method Not Allowed").setHeader("Allow", allowHeader);
    }

    private EntityWriteOperation entityOperationFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ThingifierRequestContext context) {
        if (verb == RoutingVerb.POST && route instanceof CollectionRoute) {
            return EntityWriteOperation.CREATE;
        }
        if ((verb == RoutingVerb.POST || verb == RoutingVerb.PATCH)
                && route instanceof InstanceRoute) {
            return EntityWriteOperation.UPDATE;
        }
        if (verb == RoutingVerb.PUT && route instanceof InstanceRoute) {
            return entityInstanceExists((InstanceRoute) route, context)
                    ? EntityWriteOperation.UPDATE
                    : EntityWriteOperation.CREATE;
        }
        return null;
    }

    private boolean entityInstanceExists(
            final InstanceRoute route, final ThingifierRequestContext context) {
        EntityDefinition entity =
                runtime.schema().definitionWithSingularOrPluralNamed(route.entity().name());
        if (entity == null) {
            return false;
        }
        EntityInstance found =
                context.store().entityQueries().findByQueryIdentifier(entity, route.identifier());
        return found != null;
    }

    private RelationshipWriteOperation relationshipOperationFor(
            final RoutingVerb verb, final ThingRoute route, final ApiBodyFields bodyFields) {
        if (verb == RoutingVerb.DELETE && route instanceof RelationshipInstanceRoute) {
            return RelationshipWriteOperation.DISCONNECT;
        }
        if (verb == RoutingVerb.POST && route instanceof RelationshipCollectionRoute) {
            return bodyReferencesExistingRelatedItem(
                            (RelationshipCollectionRoute) route, bodyFields)
                    ? RelationshipWriteOperation.CONNECT_EXISTING
                    : RelationshipWriteOperation.CREATE_AND_CONNECT;
        }
        return null;
    }

    private boolean bodyReferencesExistingRelatedItem(
            final RelationshipCollectionRoute route, final ApiBodyFields bodyFields) {
        EntityDefinition targetEntity = targetEntityFor(route);
        if (targetEntity == null) {
            return false;
        }

        for (java.util.Map.Entry<String, String> entry : bodyFields.asFlattenedStringMap()) {
            Field field = targetEntity.getField(entry.getKey());
            if (field != null
                    && (field.getType() == FieldType.AUTO_GUID
                            || field.getType() == FieldType.AUTO_INCREMENT)) {
                return true;
            }
        }
        return false;
    }

    private EntityDefinition targetEntityFor(final RelationshipCollectionRoute route) {
        for (RelationshipSpec relationship : route.parentEntity().relationships()) {
            if (relationship.name().equals(route.relationshipName())) {
                return runtime.schema()
                        .definitionWithSingularOrPluralNamed(relationship.toEntityName());
            }
        }
        return null;
    }

    private Set<EntityWriteOperation> entityOperationsFor(
            final RoutingVerb verb, final ThingRoute route) {
        return runtime.apiSpec()
                .entityWriteOperationsFor(
                        verb, route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().entities().operationsFor(verb));
    }

    public Set<EntityPatchUpdateStyle> entityPatchUpdateStylesFor(final ThingRoute route) {
        return runtime.apiSpec()
                .entityPatchUpdateStylesFor(
                        route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().entities().patchUpdateStyles());
    }

    private Set<RelationshipWriteOperation> relationshipOperationsFor(
            final RoutingVerb verb, final ThingRoute route) {
        return runtime.apiSpec()
                .relationshipWriteOperationsFor(
                        verb, route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().relationships().operationsFor(verb));
    }

    private String allowHeaderFor(
            final ThingRoute route,
            final ThingifierRequestContext context,
            final RoutingVerb blockedVerb,
            final EntityWriteOperation blockedOperation) {
        List<String> allowed = new ArrayList<>();
        allowed.add("OPTIONS");
        if (route instanceof CollectionRoute) {
            allowed.add("GET");
            allowed.add("HEAD");
            if (entityOperationsFor(RoutingVerb.POST, route)
                    .contains(EntityWriteOperation.CREATE)) {
                allowed.add("POST");
            }
            allowed.add("QUERY");
        }
        if (route instanceof InstanceRoute) {
            allowed.add("GET");
            allowed.add("HEAD");
            if (entityOperationsFor(RoutingVerb.POST, route)
                    .contains(EntityWriteOperation.UPDATE)) {
                allowed.add("POST");
            }
            if (isEntityMethodAllowedFor(
                    RoutingVerb.PUT, route, context, blockedVerb, blockedOperation)) {
                allowed.add("PUT");
            }
            if (!entityPatchUpdateStylesFor(route).isEmpty()) {
                allowed.add("PATCH");
            }
            allowed.add("DELETE");
        }
        return String.join(", ", allowed);
    }

    private boolean isEntityMethodAllowedFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ThingifierRequestContext context,
            final RoutingVerb blockedVerb,
            final EntityWriteOperation blockedOperation) {
        EntityWriteOperation operation =
                verb == blockedVerb ? blockedOperation : entityOperationFor(verb, route, context);
        return operation != null && entityOperationsFor(verb, route).contains(operation);
    }

    private String allowHeaderFor(
            final ThingRoute route,
            final RoutingVerb blockedVerb,
            final RelationshipWriteOperation blockedOperation) {
        List<String> allowed = new ArrayList<>();
        allowed.add("OPTIONS");
        if (route instanceof RelationshipCollectionRoute) {
            allowed.add("GET");
            allowed.add("HEAD");
            if (isRelationshipMethodAllowedFor(
                    RoutingVerb.POST, route, blockedVerb, blockedOperation)) {
                allowed.add("POST");
            }
            allowed.add("QUERY");
        }
        if (route instanceof RelationshipInstanceRoute) {
            if (isRelationshipMethodAllowedFor(
                    RoutingVerb.DELETE, route, blockedVerb, blockedOperation)) {
                allowed.add("DELETE");
            }
        }
        return String.join(", ", allowed);
    }

    private boolean isRelationshipMethodAllowedFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final RoutingVerb blockedVerb,
            final RelationshipWriteOperation blockedOperation) {
        RelationshipWriteOperation operation =
                verb == blockedVerb
                        ? blockedOperation
                        : relationshipOperationFor(verb, route, ApiBodyFields.empty());
        return operation != null && relationshipOperationsFor(verb, route).contains(operation);
    }
}
