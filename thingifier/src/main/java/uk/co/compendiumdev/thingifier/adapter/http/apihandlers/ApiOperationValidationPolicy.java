package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidationContext;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidationResult;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidatorDefinition;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Executes route-level API operation validators for a resolved request.
 *
 * <p>The policy sits between Thingifier's request/route/write mapping checks and repository model
 * validation. That makes it suitable for request-aware API rules while preserving the existing
 * field, instance, domain, and global validation pipeline for entity consistency.
 */
public final class ApiOperationValidationPolicy {

    private final ThingifierApiRuntime runtime;

    /**
     * Creates an operation validation policy for one API runtime.
     *
     * @param runtime runtime services used to resolve route rules and entity metadata
     */
    public ApiOperationValidationPolicy(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Rejects a request when one of the route's operation validators rejects it.
     *
     * <p>Validators run in declaration order. Later validators, lifecycle validation hooks, model
     * validators, and mutation are skipped after the first rejection.
     *
     * @param verb routing verb being processed
     * @param publicPath public API path requested by the caller
     * @param route resolved Thingifier route target
     * @param context active request context after auth and data-scope selection
     * @param bodyFields parsed request body fields
     * @param rawBody raw request body text
     * @param queryParams parsed query parameters
     * @param operationType resolved operation type label
     * @return rejection response, or null when validation accepts the operation
     */
    public ApiResponse rejectIfInvalid(
            final RoutingVerb verb,
            final String publicPath,
            final ThingRoute route,
            final ThingifierRequestContext context,
            final ApiBodyFields bodyFields,
            final String rawBody,
            final QueryFilterParams queryParams,
            final String operationType) {
        Optional<ThingifierApiRouteRule> routeRule = routeRuleFor(verb, route, publicPath);
        if (routeRule.isEmpty() || !routeRule.get().hasApiOperationValidators()) {
            return null;
        }

        ApiOperationValidationContext validationContext =
                validationContextFor(
                        verb,
                        publicPath,
                        route,
                        context,
                        bodyFields,
                        rawBody,
                        queryParams,
                        operationType);

        for (ApiOperationValidatorDefinition definition :
                routeRule.get().apiOperationValidators()) {
            ApiOperationValidationResult result =
                    definition.validator().validate(validationContext);
            if (result != null && result.rejected()) {
                return ApiResponse.validationError(result.statusCode(), result.message());
            }
        }
        return null;
    }

    private Optional<ThingifierApiRouteRule> routeRuleFor(
            final RoutingVerb verb, final ThingRoute route, final String publicPath) {
        final String path = route == null ? publicPath : route.originalPath();
        return runtime.apiSpec().ruleFor(verb, path, runtime.apiConfig().getApiEndPointPrefix());
    }

    private ApiOperationValidationContext validationContextFor(
            final RoutingVerb verb,
            final String publicPath,
            final ThingRoute route,
            final ThingifierRequestContext context,
            final ApiBodyFields bodyFields,
            final String rawBody,
            final QueryFilterParams queryParams,
            final String operationType) {
        EntityDefinition targetEntity = targetEntityFor(route);
        String targetEntityName = targetEntity == null ? null : targetEntity.getName();
        String targetIdentifier = targetIdentifierFor(route);
        String requestView = requestEntityViewFor(verb, route, targetEntity);
        String responseView = responseEntityViewFor(verb, route, targetEntity, operationType);

        return new ApiOperationValidationContext(
                verb,
                publicPath,
                route,
                targetEntityName,
                targetIdentifier,
                operationType,
                context,
                context == null ? null : context.headers(),
                queryParams,
                bodyFields,
                rawBody,
                requestView,
                responseView);
    }

    private EntityDefinition targetEntityFor(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return entityNamed(((CollectionRoute) route).entity().name());
        }
        if (route instanceof InstanceRoute) {
            return entityNamed(((InstanceRoute) route).entity().name());
        }
        if (route instanceof RelationshipCollectionRoute) {
            RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            return targetEntityForRelationship(
                    relationship.parentEntity(), relationship.relationshipName());
        }
        if (route instanceof RelationshipInstanceRoute) {
            RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
            return targetEntityForRelationship(
                    relationship.parentEntity(), relationship.relationshipName());
        }
        return null;
    }

    private String targetIdentifierFor(final ThingRoute route) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    private EntityDefinition targetEntityForRelationship(
            final EntityTypeRef parentEntity, final String relationshipName) {
        if (parentEntity == null) {
            return null;
        }
        for (RelationshipSpec spec : parentEntity.relationships()) {
            if (spec.name().equals(relationshipName)) {
                return entityNamed(spec.toEntityName());
            }
        }
        return null;
    }

    private EntityDefinition entityNamed(final String entityName) {
        return runtime.schema().definitionWithSingularOrPluralNamed(entityName);
    }

    private String requestEntityViewFor(
            final RoutingVerb verb, final ThingRoute route, final EntityDefinition targetEntity) {
        if (route == null) {
            return null;
        }
        Optional<ThingifierApiRouteRule> rule = routeRuleFor(verb, route, route.originalPath());
        if (targetEntity == null) {
            return rule.filter(ThingifierApiRouteRule::hasRequestEntityView)
                    .map(ThingifierApiRouteRule::getRequestEntityView)
                    .orElse(null);
        }
        return runtime.apiSpec()
                .requestEntityViewFor(
                        verb,
                        route.originalPath(),
                        runtime.apiConfig().getApiEndPointPrefix(),
                        targetEntity)
                .orElse(null);
    }

    private String responseEntityViewFor(
            final RoutingVerb verb,
            final ThingRoute route,
            final EntityDefinition targetEntity,
            final String operationType) {
        if (route == null || targetEntity == null) {
            return null;
        }
        return runtime.apiSpec()
                .responseEntityViewFor(
                        verb,
                        route.originalPath(),
                        runtime.apiConfig().getApiEndPointPrefix(),
                        targetEntity,
                        successStatusFor(verb, operationType))
                .orElse(null);
    }

    private int successStatusFor(final RoutingVerb verb, final String operationType) {
        if (verb == RoutingVerb.DELETE
                || "DELETE".equals(operationType)
                || "DISCONNECT".equals(operationType)) {
            return 204;
        }
        if ("CREATE".equals(operationType)
                || "CREATE_AND_CONNECT".equals(operationType)
                || "CONNECT_EXISTING".equals(operationType)) {
            return 201;
        }
        return 200;
    }
}
