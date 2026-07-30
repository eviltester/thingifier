package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.Set;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.SchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;

public final class WriteMethodRoutePolicy {

    private final Thingifier thingifier;
    private final SchemaCatalog schema;

    public WriteMethodRoutePolicy(final Thingifier thingifier) {
        this.thingifier = thingifier;
        this.schema = new ThingifierSchemaCatalog(thingifier);
    }

    public void applyTo(final ApiRoutingDefinition routingDefinition, final String apiPathPrefix) {
        for (RoutingDefinition route : routingDefinition.definitions()) {
            ThingRoute thingRoute =
                    new ThingRouteMapper(schema).map(removePrefix(route.url(), apiPathPrefix));
            applyTo(route, thingRoute, apiPathPrefix);
        }
        updateAcceptPatchHeaders(routingDefinition, apiPathPrefix);
    }

    private void applyTo(
            final RoutingDefinition route,
            final ThingRoute thingRoute,
            final String apiPathPrefix) {
        if (thingRoute instanceof CollectionRoute || thingRoute instanceof InstanceRoute) {
            applyEntityPolicy(route, thingRoute, apiPathPrefix);
        }
        if (thingRoute instanceof RelationshipCollectionRoute
                || thingRoute instanceof RelationshipInstanceRoute) {
            applyRelationshipPolicy(route, thingRoute, apiPathPrefix);
        }
    }

    private void applyEntityPolicy(
            final RoutingDefinition route,
            final ThingRoute thingRoute,
            final String apiPathPrefix) {
        if (route.verb() == RoutingVerb.POST && thingRoute instanceof CollectionRoute) {
            setEntityRouteSupport(
                    route,
                    thingRoute,
                    apiPathPrefix,
                    EntityWriteOperation.CREATE,
                    ((CollectionRoute) thingRoute).entity().name(),
                    true);
        }
        if (route.verb() == RoutingVerb.POST && thingRoute instanceof InstanceRoute) {
            setEntityRouteSupport(
                    route,
                    thingRoute,
                    apiPathPrefix,
                    EntityWriteOperation.UPDATE,
                    ((InstanceRoute) thingRoute).entity().name(),
                    false);
        }
        if (route.verb() == RoutingVerb.PUT && thingRoute instanceof InstanceRoute) {
            Set<EntityWriteOperation> operations =
                    entityOperationsFor(route.verb(), thingRoute, apiPathPrefix);
            if (operations.isEmpty()) {
                methodNotAllowed(route);
            } else {
                returnedEntityPutRoute(
                        route, ((InstanceRoute) thingRoute).entity().name(), operations);
            }
        }
        if (route.verb() == RoutingVerb.PATCH && thingRoute instanceof InstanceRoute) {
            Set<EntityPatchUpdateStyle> styles =
                    entityPatchUpdateStylesFor(thingRoute, apiPathPrefix);
            if (styles.isEmpty()) {
                methodNotAllowed(route);
            } else {
                returnedEntityWriteRoute(
                        route, ((InstanceRoute) thingRoute).entity().name(), false);
                ensureStatus(route, 400);
                ensureStatus(route, 415);
                route.requestContentTypes(mediaTypesFor(styles));
            }
        }
    }

    private void setEntityRouteSupport(
            final RoutingDefinition route,
            final ThingRoute thingRoute,
            final String apiPathPrefix,
            final EntityWriteOperation operation,
            final String entityName,
            final boolean createRoute) {
        if (entityOperationsFor(route.verb(), thingRoute, apiPathPrefix).contains(operation)) {
            returnedEntityWriteRoute(route, entityName, createRoute);
        } else {
            methodNotAllowed(route);
        }
    }

    private void returnedEntityWriteRoute(
            final RoutingDefinition route, final String entityName, final boolean createRoute) {
        route.replaceStatus(RoutingStatus.returnedFromCall());
        if (createRoute) {
            ensureStatus(route, 201);
            route.returnPayload(201, entityName);
            route.requestPayload("create_" + entityName);
            return;
        }
        ensureStatus(route, 200);
        ensureStatus(route, 404);
        ensureStatus(route, 422);
        ensureStatus(route, 409);
        route.returnPayload(200, entityName);
        route.requestPayload(entityName);
        replaceMethodNotAllowedDocumentation(
                route,
                String.format(
                        "patch a specific instance of %s with a body containing the patch details",
                        entityName));
    }

    private void returnedEntityPutRoute(
            final RoutingDefinition route,
            final String entityName,
            final Set<EntityWriteOperation> operations) {
        route.replaceStatus(RoutingStatus.returnedFromCall());
        route.clearPossibleStatuses();
        route.clearReturnPayloads();
        if (operations.contains(EntityWriteOperation.CREATE)) {
            ensureStatus(route, 201);
            route.returnPayload(201, entityName);
        }
        if (operations.contains(EntityWriteOperation.UPDATE)) {
            ensureStatus(route, 200);
            ensureStatus(route, 404);
            route.returnPayload(200, entityName);
        }
        ensureStatus(route, 422);
        ensureStatus(route, 409);
        route.requestPayload(entityName);
    }

    private void applyRelationshipPolicy(
            final RoutingDefinition route,
            final ThingRoute thingRoute,
            final String apiPathPrefix) {
        if (route.verb() == RoutingVerb.POST && thingRoute instanceof RelationshipCollectionRoute) {
            Set<RelationshipWriteOperation> operations =
                    relationshipOperationsFor(route.verb(), thingRoute, apiPathPrefix);
            if (operations.contains(RelationshipWriteOperation.CREATE_AND_CONNECT)
                    || operations.contains(RelationshipWriteOperation.CONNECT_EXISTING)) {
                route.replaceStatus(RoutingStatus.returnedFromCall());
            } else {
                methodNotAllowed(route);
            }
        }
        if (route.verb() == RoutingVerb.DELETE && thingRoute instanceof RelationshipInstanceRoute) {
            Set<RelationshipWriteOperation> operations =
                    relationshipOperationsFor(route.verb(), thingRoute, apiPathPrefix);
            if (operations.contains(RelationshipWriteOperation.DISCONNECT)) {
                route.replaceStatus(RoutingStatus.returnedFromCall());
            } else {
                methodNotAllowed(route);
            }
        }
    }

    private Set<EntityWriteOperation> entityOperationsFor(
            final RoutingVerb verb, final ThingRoute route, final String apiPathPrefix) {
        return thingifier
                .apiSpec()
                .entityWriteOperationsFor(verb, route.originalPath(), apiPathPrefix)
                .orElse(thingifier.apiConfig().writeMethods().entities().operationsFor(verb));
    }

    private Set<EntityPatchUpdateStyle> entityPatchUpdateStylesFor(
            final ThingRoute route, final String apiPathPrefix) {
        return thingifier
                .apiSpec()
                .entityPatchUpdateStylesFor(route.originalPath(), apiPathPrefix)
                .orElse(thingifier.apiConfig().writeMethods().entities().patchUpdateStyles());
    }

    private Set<RelationshipWriteOperation> relationshipOperationsFor(
            final RoutingVerb verb, final ThingRoute route, final String apiPathPrefix) {
        return thingifier
                .apiSpec()
                .relationshipWriteOperationsFor(verb, route.originalPath(), apiPathPrefix)
                .orElse(thingifier.apiConfig().writeMethods().relationships().operationsFor(verb));
    }

    private void methodNotAllowed(final RoutingDefinition route) {
        route.replaceStatus(RoutingStatus.returnValue(405));
    }

    private void replaceMethodNotAllowedDocumentation(
            final RoutingDefinition route, final String documentation) {
        if ("method not allowed".equals(route.getDocumentation())) {
            route.addDocumentation(documentation);
        }
    }

    private void ensureStatus(final RoutingDefinition route, final int statusCode) {
        for (RoutingStatus status : route.getPossibleStatusReponses()) {
            if (status.value() == statusCode) {
                return;
            }
        }
        route.addPossibleStatus(RoutingStatus.returnValue(statusCode));
    }

    private void updateAcceptPatchHeaders(
            final ApiRoutingDefinition routingDefinition, final String apiPathPrefix) {
        for (RoutingDefinition route : routingDefinition.definitions()) {
            if (route.verb() != RoutingVerb.OPTIONS) {
                continue;
            }

            ThingRoute thingRoute =
                    new ThingRouteMapper(schema).map(removePrefix(route.url(), apiPathPrefix));
            if (!(thingRoute instanceof InstanceRoute)) {
                continue;
            }

            Set<EntityPatchUpdateStyle> styles =
                    entityPatchUpdateStylesFor(thingRoute, apiPathPrefix);
            if (!styles.isEmpty()) {
                route.addResponseHeader(
                        "Accept-Patch", EntityPatchUpdateStyle.acceptPatchHeaderValue(styles));
            }
        }
    }

    private String[] mediaTypesFor(final Set<EntityPatchUpdateStyle> styles) {
        return styles.stream()
                .sorted()
                .map(EntityPatchUpdateStyle::mediaType)
                .toArray(String[]::new);
    }

    private String removePrefix(final String path, final String apiPathPrefix) {
        final String normalizedPath = normalize(path);
        final String normalizedPrefix = normalize(apiPathPrefix);
        if (normalizedPrefix.isEmpty()) {
            return normalizedPath;
        }
        if (normalizedPath.equals(normalizedPrefix)) {
            return "";
        }
        if (normalizedPath.startsWith(normalizedPrefix + "/")) {
            return normalizedPath.substring(normalizedPrefix.length() + 1);
        }
        return normalizedPath;
    }

    private String normalize(final String path) {
        String normalized = path == null ? "" : path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
