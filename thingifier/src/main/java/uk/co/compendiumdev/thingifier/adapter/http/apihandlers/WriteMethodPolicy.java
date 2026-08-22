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
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Enforces write-method policy for generated Thingifier routes.
 *
 * <p>The policy combines global write-method configuration with API spec route overrides such as
 * {@code methodNotAllowed}. It returns 405 responses before write commands are validated or applied
 * when the generated route should not perform the requested operation.
 */
public final class WriteMethodPolicy {

    private final ThingifierApiRuntime runtime;

    /**
     * Creates a policy checker backed by runtime configuration and API spec rules.
     *
     * @param runtime runtime services for config, spec, schema, and store access
     */
    public WriteMethodPolicy(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Rejects a write request when the generated route is not allowed to perform its operation.
     *
     * @param verb routing verb being processed
     * @param route mapped generated route
     * @param bodyFields parsed request body fields
     * @param context request context used to inspect existing data when needed
     * @return rejection response, or null when the write may continue
     */
    public ApiResponse rejectIfNotAllowed(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        ApiResponse invalidConfig = rejectInvalidApiConfig();
        if (invalidConfig != null) {
            return invalidConfig;
        }

        ApiResponse configuredMethodNotAllowed =
                rejectIfMethodNotAllowedByApiSpec(verb, route, bodyFields, context);
        if (configuredMethodNotAllowed != null) {
            return configuredMethodNotAllowed;
        }

        if (route instanceof CollectionRoute || route instanceof InstanceRoute) {
            return rejectEntityWriteIfNotAllowed(verb, route, bodyFields, context);
        }

        if (route instanceof RelationshipCollectionRoute
                || route instanceof RelationshipInstanceRoute) {
            return rejectRelationshipWriteIfNotAllowed(verb, route, bodyFields, context);
        }

        return null;
    }

    /**
     * Rejects a route when the API spec declares the verb as method-not-allowed.
     *
     * <p>This check runs before operation-specific write policy because a route-level public API
     * decision should win over generated create/update/connect semantics.
     *
     * @param verb routing verb being processed
     * @param route mapped generated route
     * @param bodyFields parsed request body fields
     * @param context request context used for Allow header calculation
     * @return rejection response, or null when the API spec allows the method
     */
    private ApiResponse rejectIfMethodNotAllowedByApiSpec(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (!isMethodNotAllowedByApiSpec(verb, route)) {
            return null;
        }
        return methodNotAllowed(allowHeaderFor(route, bodyFields, context));
    }

    /**
     * Rejects entity writes whose concrete create/update operation is not allowed.
     *
     * @param verb routing verb being processed
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @param context request context used to inspect existing data when needed
     * @return rejection response, or null when the entity write may continue
     */
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

    /**
     * Rejects PATCH requests when the route or API config does not accept the requested shape.
     *
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @param context request context used for Allow header calculation
     * @return rejection response, or null when PATCH may continue
     */
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

    /**
     * Rejects relationship writes whose connect/disconnect operation is not allowed.
     *
     * @param verb routing verb being processed
     * @param route mapped relationship route
     * @param bodyFields parsed request body fields
     * @return rejection response, or null when the relationship write may continue
     */
    private ApiResponse rejectRelationshipWriteIfNotAllowed(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        Set<RelationshipWriteOperation> allowed = relationshipOperationsFor(verb, route);
        RelationshipWriteOperation operation =
                relationshipOperationFor(verb, route, bodyFields, context, allowed);
        if (operation == null) {
            return null;
        }

        if (allowed.contains(operation)) {
            return null;
        }

        return methodNotAllowed(allowHeaderFor(route, verb, operation));
    }

    /**
     * Creates Thingifier's standard 405 response.
     *
     * @param allowHeader value to expose in the Allow header
     * @return method-not-allowed API response
     */
    private ApiResponse methodNotAllowed(final String allowHeader) {
        return ApiResponse.error(405, "Method Not Allowed").setHeader("Allow", allowHeader);
    }

    /**
     * Rejects requests when global API write-method configuration is invalid.
     *
     * @return server error response, or null when configuration is valid
     */
    private ApiResponse rejectInvalidApiConfig() {
        ApiConfigValidationReport validation = runtime.apiConfig().validate();
        if (validation.isValid()) {
            return null;
        }
        return ApiResponse.error(500, validation.errorMessages());
    }

    /**
     * Determines the entity operation represented by a generated write route.
     *
     * @param verb routing verb being processed
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @param context request context used to check whether a PUT target already exists
     * @return create/update operation, or null when the route does not map to one
     */
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

    /**
     * Reports whether an entity instance already exists for the supplied identifier.
     *
     * @param entityRef entity reference from the route
     * @param identifier route or payload identifier
     * @param context request context containing the active store
     * @return true when the instance exists
     */
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

    /**
     * Extracts the entity reference from a collection or instance route.
     *
     * @param route mapped entity route
     * @return entity reference from the route
     */
    private EntityTypeRef entityFor(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return ((CollectionRoute) route).entity();
        }
        return ((InstanceRoute) route).entity();
    }

    /**
     * Resolves the identifier PUT would use for create-or-update decisions.
     *
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @return identifier from URI or payload, or null when none is available
     */
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

    /**
     * Reports whether a PUT route can legally derive an identifier from its URI or payload.
     *
     * @param route mapped entity route
     * @return true when PUT identifier policy permits this route shape
     */
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

    /**
     * Reports whether an identifier has usable text.
     *
     * @param identifier candidate identifier
     * @return true when the identifier is not blank
     */
    private boolean hasIdentifier(final String identifier) {
        return identifier != null && !identifier.trim().isEmpty();
    }

    /**
     * Determines the relationship operation represented by a generated relationship route.
     *
     * @param verb routing verb being processed
     * @param route mapped relationship route
     * @param bodyFields parsed request body fields
     * @return relationship operation, or null when the route does not map to one
     */
    private RelationshipWriteOperation relationshipOperationFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final Set<RelationshipWriteOperation> allowedOperations) {
        return new RelationshipWriteIntentResolver(runtime.schema())
                .intentFor(verb, route, bodyFields, context, allowedOperations)
                .operation();
    }

    /**
     * Resolves allowed entity operations from API spec or global write-method config.
     *
     * @param verb routing verb being processed
     * @param route mapped entity route
     * @return allowed entity operations
     */
    private Set<EntityWriteOperation> entityOperationsFor(
            final RoutingVerb verb, final ThingRoute route) {
        return runtime.apiSpec()
                .entityWriteOperationsFor(
                        verb, route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().entities().operationsFor(verb));
    }

    /**
     * Resolves allowed PATCH styles from API spec or global write-method config.
     *
     * @param route mapped entity route
     * @return accepted PATCH update styles
     */
    public Set<EntityPatchUpdateStyle> entityPatchUpdateStylesFor(final ThingRoute route) {
        return runtime.apiSpec()
                .entityPatchUpdateStylesFor(
                        route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().entities().patchUpdateStyles());
    }

    /**
     * Resolves allowed relationship operations from API spec or global write-method config.
     *
     * @param verb routing verb being processed
     * @param route mapped relationship route
     * @return allowed relationship operations
     */
    public Set<RelationshipWriteOperation> relationshipOperationsFor(
            final RoutingVerb verb, final ThingRoute route) {
        return runtime.apiSpec()
                .relationshipWriteOperationsFor(
                        verb, route.originalPath(), runtime.apiConfig().getApiEndPointPrefix())
                .orElse(runtime.apiConfig().writeMethods().relationships().operationsFor(verb));
    }

    /**
     * Resolves the concrete write operation represented by the current route and payload.
     *
     * <p>Operation validators receive this value so they can reason about the public API operation
     * after Thingifier has applied the same create/update/connect intent rules used by write-policy
     * enforcement.
     *
     * @param verb routing verb being processed
     * @param route mapped generated route
     * @param bodyFields parsed request body fields
     * @param context request context used to inspect existing data when needed
     * @return operation label such as CREATE, UPDATE, CREATE_AND_CONNECT, DISCONNECT, or DELETE
     */
    public String operationTypeFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        if (route instanceof CollectionRoute || route instanceof InstanceRoute) {
            if (verb == RoutingVerb.DELETE && route instanceof InstanceRoute) {
                return "DELETE";
            }
            EntityWriteOperation operation = entityOperationFor(verb, route, bodyFields, context);
            if (operation != null) {
                return operation.name();
            }
        }

        if (route instanceof RelationshipCollectionRoute
                || route instanceof RelationshipInstanceRoute) {
            RelationshipWriteOperation operation =
                    relationshipOperationFor(
                            verb,
                            route,
                            bodyFields,
                            context,
                            relationshipOperationsFor(verb, route));
            if (operation != null) {
                return operation.name();
            }
        }

        return verb.name();
    }

    /**
     * Builds an Allow header while excluding a rejected entity write operation.
     *
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @param context request context used for PUT create/update decisions
     * @param blockedVerb verb currently being rejected
     * @param blockedOperation operation currently being rejected
     * @return comma-separated Allow header value
     */
    private String allowHeaderFor(
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context,
            final RoutingVerb blockedVerb,
            final EntityWriteOperation blockedOperation) {
        List<String> allowed = new ArrayList<>();
        allowed.add("OPTIONS");
        if (route instanceof CollectionRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (entityOperationsFor(RoutingVerb.POST, route).contains(EntityWriteOperation.CREATE)
                    && isMethodAllowedByApiSpec(RoutingVerb.POST, route)) {
                allowed.add("POST");
            }
            if (isEntityMethodAllowedFor(
                    RoutingVerb.PUT, route, bodyFields, context, blockedVerb, blockedOperation)) {
                allowed.add("PUT");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.QUERY, route)) {
                allowed.add("QUERY");
            }
        }
        if (route instanceof InstanceRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (entityOperationsFor(RoutingVerb.POST, route).contains(EntityWriteOperation.UPDATE)
                    && isMethodAllowedByApiSpec(RoutingVerb.POST, route)) {
                allowed.add("POST");
            }
            if (isEntityMethodAllowedFor(
                    RoutingVerb.PUT, route, bodyFields, context, blockedVerb, blockedOperation)) {
                allowed.add("PUT");
            }
            if (!entityPatchUpdateStylesFor(route).isEmpty()
                    && isMethodAllowedByApiSpec(RoutingVerb.PATCH, route)) {
                allowed.add("PATCH");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.DELETE, route)) {
                allowed.add("DELETE");
            }
        }
        return String.join(", ", allowed);
    }

    /**
     * Builds an Allow header from all methods currently permitted for a generated route.
     *
     * @param route mapped route
     * @param bodyFields parsed request body fields
     * @param context request context used for PUT create/update decisions
     * @return comma-separated Allow header value
     */
    private String allowHeaderFor(
            final ThingRoute route,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        List<String> allowed = new ArrayList<>();
        allowed.add("OPTIONS");
        if (route instanceof CollectionRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (entityOperationsFor(RoutingVerb.POST, route).contains(EntityWriteOperation.CREATE)
                    && isMethodAllowedByApiSpec(RoutingVerb.POST, route)) {
                allowed.add("POST");
            }
            if (isEntityMethodAllowedFor(RoutingVerb.PUT, route, bodyFields, context, null, null)) {
                allowed.add("PUT");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.QUERY, route)) {
                allowed.add("QUERY");
            }
        }
        if (route instanceof InstanceRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (entityOperationsFor(RoutingVerb.POST, route).contains(EntityWriteOperation.UPDATE)
                    && isMethodAllowedByApiSpec(RoutingVerb.POST, route)) {
                allowed.add("POST");
            }
            if (isEntityMethodAllowedFor(RoutingVerb.PUT, route, bodyFields, context, null, null)) {
                allowed.add("PUT");
            }
            if (!entityPatchUpdateStylesFor(route).isEmpty()
                    && isMethodAllowedByApiSpec(RoutingVerb.PATCH, route)) {
                allowed.add("PATCH");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.DELETE, route)) {
                allowed.add("DELETE");
            }
        }
        if (route instanceof RelationshipCollectionRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (isRelationshipMethodAllowedFor(RoutingVerb.POST, route, null, null)) {
                allowed.add("POST");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.QUERY, route)) {
                allowed.add("QUERY");
            }
        }
        if (route instanceof RelationshipInstanceRoute
                && isRelationshipMethodAllowedFor(RoutingVerb.DELETE, route, null, null)) {
            allowed.add("DELETE");
        }
        return String.join(", ", allowed);
    }

    /**
     * Reports whether a specific entity method is allowed for the route and current data.
     *
     * @param verb routing verb to check
     * @param route mapped entity route
     * @param bodyFields parsed request body fields
     * @param context request context used for PUT create/update decisions
     * @param blockedVerb verb currently being rejected, or null
     * @param blockedOperation operation currently being rejected, or null
     * @return true when the method should appear in Allow
     */
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
        if (!isMethodAllowedByApiSpec(verb, route)) {
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

    /**
     * Builds an Allow header while excluding a rejected relationship write operation.
     *
     * @param route mapped relationship route
     * @param blockedVerb verb currently being rejected
     * @param blockedOperation operation currently being rejected
     * @return comma-separated Allow header value
     */
    private String allowHeaderFor(
            final ThingRoute route,
            final RoutingVerb blockedVerb,
            final RelationshipWriteOperation blockedOperation) {
        List<String> allowed = new ArrayList<>();
        allowed.add("OPTIONS");
        if (route instanceof RelationshipCollectionRoute) {
            if (isMethodAllowedByApiSpec(RoutingVerb.GET, route)) {
                allowed.add("GET");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.HEAD, route)) {
                allowed.add("HEAD");
            }
            if (isRelationshipMethodAllowedFor(
                    RoutingVerb.POST, route, blockedVerb, blockedOperation)) {
                allowed.add("POST");
            }
            if (isMethodAllowedByApiSpec(RoutingVerb.QUERY, route)) {
                allowed.add("QUERY");
            }
        }
        if (route instanceof RelationshipInstanceRoute) {
            if (isRelationshipMethodAllowedFor(
                    RoutingVerb.DELETE, route, blockedVerb, blockedOperation)) {
                allowed.add("DELETE");
            }
        }
        return String.join(", ", allowed);
    }

    /**
     * Reports whether a specific relationship method is allowed for the route.
     *
     * @param verb routing verb to check
     * @param route mapped relationship route
     * @param blockedVerb verb currently being rejected, or null
     * @param blockedOperation operation currently being rejected, or null
     * @return true when the method should appear in Allow
     */
    private boolean isRelationshipMethodAllowedFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final RoutingVerb blockedVerb,
            final RelationshipWriteOperation blockedOperation) {
        if (!isMethodAllowedByApiSpec(verb, route)) {
            return false;
        }
        if (verb == RoutingVerb.POST && route instanceof RelationshipCollectionRoute) {
            return !relationshipOperationsFor(verb, route).isEmpty();
        }

        RelationshipWriteOperation operation =
                verb == blockedVerb
                        ? blockedOperation
                        : relationshipOperationFor(
                                verb,
                                route,
                                ApiBodyFields.empty(),
                                null,
                                relationshipOperationsFor(verb, route));
        return operation != null && relationshipOperationsFor(verb, route).contains(operation);
    }

    /**
     * Reports whether API spec allows a generated method to be advertised.
     *
     * @param verb routing verb to check
     * @param route mapped route
     * @return true when the method is not configured as method-not-allowed
     */
    private boolean isMethodAllowedByApiSpec(final RoutingVerb verb, final ThingRoute route) {
        return !isMethodNotAllowedByApiSpec(verb, route);
    }

    /**
     * Reports whether API spec marks a generated method as 405 Method Not Allowed.
     *
     * @param verb routing verb to check
     * @param route mapped route
     * @return true when API spec marks the method unavailable
     */
    private boolean isMethodNotAllowedByApiSpec(final RoutingVerb verb, final ThingRoute route) {
        return runtime.apiSpec()
                .isMethodNotAllowed(
                        verb, route.originalPath(), runtime.apiConfig().getApiEndPointPrefix());
    }
}
