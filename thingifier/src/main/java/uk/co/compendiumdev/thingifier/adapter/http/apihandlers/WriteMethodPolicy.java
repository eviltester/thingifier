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
import uk.co.compendiumdev.thingifier.apiconfig.ApiConfigValidationReport;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

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
        ApiResponse invalidConfig = rejectInvalidApiConfig();
        if (invalidConfig != null) {
            return invalidConfig;
        }

        if (route instanceof CollectionRoute || route instanceof InstanceRoute) {
            return rejectEntityWriteIfNotAllowed(verb, route, bodyFields, context);
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
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (verb == RoutingVerb.PATCH) {
            return rejectEntityPatchIfNotAllowed(route, bodyFields, context);
        }

        if (verb == RoutingVerb.PUT && !canPutRouteUseIdentifier(route)) {
            return methodNotAllowed(
                    allowHeaderFor(
                            route,
                            bodyFields,
                            context,
                            RoutingVerb.PUT,
                            EntityWriteOperation.CREATE));
        }

        EntityWriteOperation operation = entityOperationFor(verb, route, bodyFields, context);
        if (operation == null) {
            return null;
        }

        Set<EntityWriteOperation> allowed = entityOperationsFor(verb, route);
        if (allowed.contains(operation)) {
            return null;
        }

        return methodNotAllowed(allowHeaderFor(route, bodyFields, context, verb, operation));
    }

    private ApiResponse rejectEntityPatchIfNotAllowed(
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (route instanceof CollectionRoute) {
            return methodNotAllowed(
                    allowHeaderFor(
                            route,
                            bodyFields,
                            context,
                            RoutingVerb.PATCH,
                            EntityWriteOperation.UPDATE));
        }

        if (route instanceof InstanceRoute && entityPatchUpdateStylesFor(route).isEmpty()) {
            return methodNotAllowed(
                    allowHeaderFor(
                            route,
                            bodyFields,
                            context,
                            RoutingVerb.PATCH,
                            EntityWriteOperation.UPDATE));
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

    private ApiResponse rejectInvalidApiConfig() {
        ApiConfigValidationReport validation = runtime.apiConfig().validate();
        if (validation.isValid()) {
            return null;
        }
        return ApiResponse.error(500, validation.errorMessages());
    }

    private EntityWriteOperation entityOperationFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (verb == RoutingVerb.POST && route instanceof CollectionRoute) {
            return EntityWriteOperation.CREATE;
        }
        if ((verb == RoutingVerb.POST || verb == RoutingVerb.PATCH)
                && route instanceof InstanceRoute) {
            return EntityWriteOperation.UPDATE;
        }
        if (verb == RoutingVerb.PUT
                && (route instanceof CollectionRoute || route instanceof InstanceRoute)) {
            String identifier = putIdentifierFor(route, bodyFields);
            if (!hasIdentifier(identifier)) {
                return null;
            }
            return entityInstanceExists(entityFor(route), identifier, context)
                    ? EntityWriteOperation.UPDATE
                    : EntityWriteOperation.CREATE;
        }
        return null;
    }

    private boolean entityInstanceExists(
            final EntityTypeRef entityRef,
            final String identifier,
            final ThingifierRequestContext context) {
        EntityDefinition entity =
                runtime.schema().definitionWithSingularOrPluralNamed(entityRef.name());
        if (entity == null) {
            return false;
        }
        return context.hasEntityInstanceWithIdentifier(entity, identifier);
    }

    private EntityTypeRef entityFor(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return ((CollectionRoute) route).entity();
        }
        return ((InstanceRoute) route).entity();
    }

    private String putIdentifierFor(final ThingRoute route, final ApiBodyFields bodyFields) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof CollectionRoute) {
            EntityTypeRef entity = ((CollectionRoute) route).entity();
            if (entity.hasPrimaryKeyField()) {
                return bodyFields.asStringMap().get(entity.primaryKeyFieldName());
            }
        }
        return null;
    }

    private boolean canPutRouteUseIdentifier(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            CollectionRoute collection = (CollectionRoute) route;
            return collection.entity().hasPrimaryKeyField()
                    && runtime.apiConfig().writeMethods().entities().putIdentifierInUri()
                            != PutIdentifierPolicy.MANDATORY
                    && runtime.apiConfig().writeMethods().entities().putIdentifierInPayload()
                            != PutIdentifierPolicy.DISALLOWED;
        }
        if (route instanceof InstanceRoute) {
            return runtime.apiConfig().writeMethods().entities().putIdentifierInUri()
                    != PutIdentifierPolicy.DISALLOWED;
        }
        return false;
    }

    private boolean hasIdentifier(final String identifier) {
        return identifier != null && !identifier.trim().isEmpty();
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
            final ApiBodyFields bodyFields,
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
            if (isEntityMethodAllowedFor(
                    RoutingVerb.PUT, route, bodyFields, context, blockedVerb, blockedOperation)) {
                allowed.add("PUT");
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
                    RoutingVerb.PUT, route, bodyFields, context, blockedVerb, blockedOperation)) {
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
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final RoutingVerb blockedVerb,
            final EntityWriteOperation blockedOperation) {
        if (verb == RoutingVerb.PUT && !canPutRouteUseIdentifier(route)) {
            return false;
        }
        EntityWriteOperation operation =
                verb == blockedVerb
                        ? blockedOperation
                        : entityOperationFor(verb, route, bodyFields, context);
        if (operation == null && verb == RoutingVerb.PUT && route instanceof CollectionRoute) {
            return !entityOperationsFor(verb, route).isEmpty();
        }
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
        if (verb == RoutingVerb.POST && route instanceof RelationshipCollectionRoute) {
            return !relationshipOperationsFor(verb, route).isEmpty();
        }

        RelationshipWriteOperation operation =
                verb == blockedVerb
                        ? blockedOperation
                        : relationshipOperationFor(verb, route, ApiBodyFields.empty());
        return operation != null && relationshipOperationsFor(verb, route).contains(operation);
    }
}
