package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Builds the shared route callback context used by route operation and final-response callbacks.
 *
 * <p>Both callback families need the same trusted route, fixed-identifier, auth principal, and
 * data-scope facts. Keeping that construction here prevents each callback phase from rediscovering
 * route details differently.
 */
final class RouteCallbackContextFactory {

    private final ThingifierApiRuntime runtime;

    /**
     * Creates a context factory for the current API runtime.
     *
     * @param runtime runtime used to find route rules and route metadata
     */
    RouteCallbackContextFactory(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Finds the route rule for a public route path.
     *
     * @param verb route verb
     * @param publicPath public request path
     * @return matching route rule, if any
     */
    Optional<ThingifierApiRouteRule> routeRuleFor(final RoutingVerb verb, final String publicPath) {
        return runtime.apiSpec()
                .ruleFor(verb, publicPath, runtime.apiConfig().getApiEndPointPrefix());
    }

    /**
     * Resolves the generated route used by callback contexts.
     *
     * @param lifecycle lifecycle context when processing an HTTP/lifecycle request
     * @param verb route verb
     * @param publicPath public request path
     * @return resolved route
     */
    ThingRoute route(
            final ThingifierApiLifecycleContext lifecycle,
            final RoutingVerb verb,
            final String publicPath) {
        return lifecycle == null ? runtime.routeFor(verb, publicPath) : lifecycle.route();
    }

    /**
     * Creates the shared immutable callback context.
     *
     * @param verb route verb
     * @param publicPath public request path
     * @param route resolved generated route
     * @param routeRule matched route rule that owns the callback
     * @param requestContext active request context, or null to derive it from lifecycle
     * @param lifecycle lifecycle context when processing an HTTP/lifecycle request
     * @param request parsed request envelope when available
     * @return callback context
     */
    ThingifierApiOperationContext contextFor(
            final RoutingVerb verb,
            final String publicPath,
            final ThingRoute route,
            final ThingifierApiRouteRule routeRule,
            final ThingifierRequestContext requestContext,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request) {
        final ThingifierRequestContext activeContext =
                requestContext != null
                        ? requestContext
                        : (lifecycle == null ? null : lifecycle.requestContext());
        return new ThingifierApiOperationContext(
                verb,
                publicPath,
                route,
                routeRule,
                targetEntityName(route, lifecycle),
                targetIdentifier(route, lifecycle),
                parentEntityName(route, lifecycle),
                parentIdentifier(route, lifecycle),
                relationshipName(route, lifecycle),
                childIdentifier(route, lifecycle),
                activeContext == null ? null : activeContext.dataScopeName(),
                activeContext == null ? null : activeContext.store(),
                activeContext == null
                        ? java.util.Map.of()
                        : activeContext.authenticatedPrincipals(),
                requestHeaders(activeContext, lifecycle, request),
                queryParams(lifecycle, request),
                bodyFields(lifecycle, request),
                rawBody(lifecycle, request),
                runtime.apiConfig());
    }

    /**
     * Resolves the operation type label for route operation callbacks.
     *
     * @param verb route verb
     * @param lifecycle lifecycle context when available
     * @return operation type label
     */
    String operationTypeFor(final RoutingVerb verb, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null && lifecycle.writeCommand() != null) {
            return operationTypeFor(lifecycle.writeCommand());
        }
        if (verb == RoutingVerb.QUERY) {
            return "QUERY";
        }
        if (verb == RoutingVerb.GET || verb == RoutingVerb.HEAD) {
            return "READ";
        }
        if (verb == RoutingVerb.DELETE) {
            return "DELETE";
        }
        if (verb == RoutingVerb.PATCH) {
            return "PATCH";
        }
        if (verb == RoutingVerb.PUT) {
            return "REPLACE";
        }
        if (verb == RoutingVerb.POST) {
            return "WRITE";
        }
        return "";
    }

    private HttpHeadersBlock requestHeaders(
            final ThingifierRequestContext requestContext,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request) {
        if (request != null) {
            return request.headers();
        }
        if (lifecycle != null) {
            return lifecycle.headers();
        }
        return requestContext == null ? new HttpHeadersBlock() : requestContext.headers();
    }

    private QueryFilterParams queryParams(
            final ThingifierApiLifecycleContext lifecycle, final ApiRequestEnvelope request) {
        if (request != null) {
            return request.queryParams();
        }
        if (lifecycle != null) {
            return lifecycle.queryParams();
        }
        return new QueryFilterParams();
    }

    private ApiBodyFields bodyFields(
            final ThingifierApiLifecycleContext lifecycle, final ApiRequestEnvelope request) {
        if (request != null) {
            return request.bodyFields();
        }
        if (lifecycle != null) {
            return lifecycle.bodyFields();
        }
        return ApiBodyFields.empty();
    }

    private String rawBody(
            final ThingifierApiLifecycleContext lifecycle, final ApiRequestEnvelope request) {
        if (request != null) {
            return request.body();
        }
        if (lifecycle != null) {
            return lifecycle.rawBody();
        }
        return "";
    }

    private String targetEntityName(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null && lifecycle.targetEntity() != null) {
            return lifecycle.targetEntity().getName();
        }
        if (route instanceof CollectionRoute) {
            return ((CollectionRoute) route).entity().name();
        }
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).entity().name();
        }
        if (route instanceof RelationshipCollectionRoute) {
            return relationshipTargetEntityName((RelationshipCollectionRoute) route);
        }
        if (route instanceof RelationshipInstanceRoute) {
            return relationshipTargetEntityName((RelationshipInstanceRoute) route);
        }
        return null;
    }

    private String targetIdentifier(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null) {
            return lifecycle.targetIdentifier();
        }
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    private String parentEntityName(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null && lifecycle.parentEntity() != null) {
            return lifecycle.parentEntity().getName();
        }
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentEntity().name();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentEntity().name();
        }
        return null;
    }

    private String parentIdentifier(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null) {
            return lifecycle.parentIdentifier();
        }
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentIdentifier();
        }
        return null;
    }

    private String relationshipName(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null) {
            return lifecycle.relationshipName();
        }
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).relationshipName();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).relationshipName();
        }
        return null;
    }

    private String childIdentifier(
            final ThingRoute route, final ThingifierApiLifecycleContext lifecycle) {
        if (lifecycle != null) {
            return lifecycle.childIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    private String relationshipTargetEntityName(final RelationshipCollectionRoute route) {
        return relationshipTargetEntityName(route.parentEntity().name(), route.relationshipName());
    }

    private String relationshipTargetEntityName(final RelationshipInstanceRoute route) {
        return relationshipTargetEntityName(route.parentEntity().name(), route.relationshipName());
    }

    private String relationshipTargetEntityName(
            final String parentEntityName, final String relationshipName) {
        final ThingRoute parentRoute = runtime.routeFor(RoutingVerb.GET, parentEntityName);
        if (!(parentRoute instanceof CollectionRoute)) {
            return null;
        }
        for (RelationshipSpec relationship :
                ((CollectionRoute) parentRoute).entity().relationships()) {
            if (relationship.name().equals(relationshipName)) {
                return relationship.toEntityName();
            }
        }
        return null;
    }

    private String operationTypeFor(final ThingWriteCommand command) {
        final String commandName = command.getClass().getSimpleName();
        switch (commandName) {
            case "CreateThingCommand":
                return "CREATE";
            case "AmendThingCommand":
                return "UPDATE";
            case "ReplaceThingCommand":
                return "REPLACE";
            case "PatchThingDocumentCommand":
                return "PATCH";
            case "DeleteThingCommand":
                return "DELETE";
            case "CreateAndConnectRelationshipCommand":
                return "CREATE_AND_CONNECT";
            case "ConnectExistingRelationshipCommand":
                return "CONNECT";
            case "UpdateConnectedRelationshipCommand":
                return "UPDATE_CONNECTED";
            case "DisconnectRelationshipCommand":
                return "DISCONNECT";
            case "RelateThingCommand":
                return "RELATE";
            default:
                return commandName;
        }
    }
}
