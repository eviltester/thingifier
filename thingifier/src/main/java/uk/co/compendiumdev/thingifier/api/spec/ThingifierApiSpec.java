package uk.co.compendiumdev.thingifier.api.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.security.SecuritySchemeNames;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticator;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Declarative configuration for the generated Thingifier API surface.
 *
 * <p>The spec records small policy rules which are applied to generated documentation and checked
 * by runtime handlers. It is the preferred place for API-shape decisions such as hiding generated
 * routes, declaring a method unavailable, limiting write operations, or choosing default entity
 * views.
 */
public final class ThingifierApiSpec {

    private final List<ThingifierApiRouteRule> routeRules;
    private final List<ThingifierApiEntityRule> entityRules;
    private final List<EntityWritePolicyRule> entityWritePolicyRules;
    private final List<EntityPatchPolicyRule> entityPatchPolicyRules;
    private final List<RelationshipWritePolicyRule> relationshipWritePolicyRules;
    private final ThingifierApiSecuritySpec securitySpec;
    private final Map<String, ThingifierApiAuthenticator> authenticators;

    public ThingifierApiSpec() {
        routeRules = new ArrayList<>();
        entityRules = new ArrayList<>();
        entityWritePolicyRules = new ArrayList<>();
        entityPatchPolicyRules = new ArrayList<>();
        relationshipWritePolicyRules = new ArrayList<>();
        securitySpec = new ThingifierApiSecuritySpec();
        authenticators = new HashMap<>();
    }

    /**
     * Creates a rule for one generated route method and path.
     *
     * <p>Use this form when a rule depends on the HTTP-style verb, such as making only POST on a
     * generated route method-not-allowed or applying a response view to one method.
     *
     * @param verb generated routing verb to configure
     * @param pathPattern generated route path pattern
     * @return mutable rule for the supplied verb and path
     */
    public ThingifierApiRouteRule route(final RoutingVerb verb, final String pathPattern) {
        final ThingifierApiRouteRule rule = new ThingifierApiRouteRule(verb, pathPattern);
        routeRules.add(rule);
        return rule;
    }

    /**
     * Creates a route rule using a string verb.
     *
     * <p>This keeps configuration convenient for callers that receive verbs from text-based
     * configuration. The verb is trimmed and matched case-insensitively against {@link
     * RoutingVerb}.
     *
     * @param verb routing verb name
     * @param pathPattern generated route path pattern
     * @return mutable rule for the supplied verb and path
     */
    public ThingifierApiRouteRule route(final String verb, final String pathPattern) {
        return route(RoutingVerb.valueOf(verb.trim().toUpperCase()), pathPattern);
    }

    /**
     * Creates a path-level rule builder for configuring multiple verbs at the same endpoint.
     *
     * <p>This is useful for API surface rules which are naturally path based, for example declaring
     * both POST and PUT on {@code /carts} as method-not-allowed.
     *
     * @param pathPattern generated route path pattern
     * @return path rule facade backed by verb-specific route rules
     */
    public ThingifierApiPathRule route(final String pathPattern) {
        return new ThingifierApiPathRule(this, pathPattern);
    }

    /**
     * Returns the security declaration builder for this API.
     *
     * <p>Security declarations give named schemes a stable place in the API contract. Route rules
     * still decide which endpoints use each scheme.
     *
     * @return security declaration builder
     */
    public ThingifierApiSecuritySpec security() {
        return securitySpec;
    }

    /**
     * Registers an authenticator for a named security scheme.
     *
     * <p>Registering an authenticator makes a named scheme enforceable at runtime when a route rule
     * selects that scheme with named Basic or Bearer auth. The route rule and security declaration
     * decide which HTTP auth mechanism is documented and parsed; this callback only supplies the
     * application-specific credential check.
     *
     * @param schemeName named security scheme
     * @param authenticator callback used to authenticate parsed credentials for the scheme
     * @return this spec so API configuration can be chained
     * @throws IllegalArgumentException when the scheme name is blank or the authenticator is null
     */
    public ThingifierApiSpec authenticator(
            final String schemeName, final ThingifierApiAuthenticator authenticator) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        if (authenticator == null) {
            throw new IllegalArgumentException("authenticator is required");
        }
        authenticators.put(normalizedSchemeName, authenticator);
        return this;
    }

    /**
     * Returns the reusable entity-level rule for a model entity.
     *
     * <p>Entity rules let a caller declare default request and response views once, with
     * route-specific rules still able to override the default for endpoints that need a different
     * contract.
     *
     * @param entityName singular or plural entity name
     * @return existing or newly-created entity rule for the supplied name
     */
    public ThingifierApiEntityRule entity(final String entityName) {
        return entityRules.stream()
                .filter(rule -> rule.sameEntityName(entityName))
                .findFirst()
                .orElseGet(
                        () -> {
                            final ThingifierApiEntityRule rule =
                                    new ThingifierApiEntityRule(entityName);
                            entityRules.add(rule);
                            return rule;
                        });
    }

    /**
     * Hides all generated collection and instance routes for an entity from documentation.
     *
     * <p>The routes remain callable. Use this when the API is intentionally available but should
     * not be advertised as part of the public contract.
     *
     * @param entityPath entity path segment, with or without slashes
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec hideEntityRoutes(final String entityPath) {
        configureEntityRoutes(entityPath, false);
        return this;
    }

    /**
     * Disables all generated collection and instance routes for an entity.
     *
     * <p>Disabled routes are treated as absent at runtime and are hidden from generated
     * documentation.
     *
     * @param entityPath entity path segment, with or without slashes
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec disableEntityRoutes(final String entityPath) {
        configureEntityRoutes(entityPath, true);
        return this;
    }

    /**
     * Hides all generated relationship routes for a parent entity and relationship name.
     *
     * @param parentEntityPath parent entity path segment, with or without slashes
     * @param relationshipName generated relationship route segment
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec hideRelationshipRoutes(
            final String parentEntityPath, final String relationshipName) {
        configureRelationshipRoutes(parentEntityPath, relationshipName, false);
        return this;
    }

    /**
     * Disables all generated relationship routes for a parent entity and relationship name.
     *
     * @param parentEntityPath parent entity path segment, with or without slashes
     * @param relationshipName generated relationship route segment
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec disableRelationshipRoutes(
            final String parentEntityPath, final String relationshipName) {
        configureRelationshipRoutes(parentEntityPath, relationshipName, true);
        return this;
    }

    /**
     * Configures which create/update semantics POST may perform for an entity path.
     *
     * <p>The policy applies to the generated collection and instance forms where POST can map to a
     * concrete write operation.
     *
     * @param entityPath entity path segment, with or without slashes
     * @param operations allowed write operations for POST
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec entityPostCan(
            final String entityPath, final EntityWriteOperation... operations) {
        configureEntityWritePolicy(RoutingVerb.POST, entityPath, operations);
        return this;
    }

    /**
     * Configures which create/update semantics PUT may perform for an entity path.
     *
     * @param entityPath entity path segment, with or without slashes
     * @param operations allowed write operations for PUT
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec entityPutCan(
            final String entityPath, final EntityWriteOperation... operations) {
        configureEntityWritePolicy(RoutingVerb.PUT, entityPath, operations);
        return this;
    }

    /**
     * Configures which PATCH document styles may update an entity path.
     *
     * @param entityPath entity path segment, with or without slashes
     * @param updateStyles accepted PATCH styles
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec entityPatchCan(
            final String entityPath, final EntityPatchUpdateStyle... updateStyles) {
        configureEntityPatchPolicy(entityPath, updateStyles);
        return this;
    }

    /**
     * Configures which relationship operations POST may perform.
     *
     * @param parentEntityPath parent entity path segment, with or without slashes
     * @param relationshipName generated relationship route segment
     * @param operations allowed relationship operations for POST
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec relationshipPostCan(
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        configureRelationshipWritePolicy(
                RoutingVerb.POST, parentEntityPath, relationshipName, operations);
        return this;
    }

    /**
     * Configures which relationship operations DELETE may perform.
     *
     * @param parentEntityPath parent entity path segment, with or without slashes
     * @param relationshipName generated relationship route segment
     * @param operations allowed relationship operations for DELETE
     * @return this spec so API configuration can be chained
     */
    public ThingifierApiSpec relationshipDeleteCan(
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        configureRelationshipWritePolicy(
                RoutingVerb.DELETE, parentEntityPath, relationshipName, operations);
        return this;
    }

    /**
     * Applies this spec to generated route documentation metadata.
     *
     * <p>Runtime handlers also consult the spec directly. This method keeps generated route
     * definitions, advertised payloads, and OPTIONS Allow headers aligned with the same policy.
     *
     * @param routingDefinition generated route definition set to update
     * @param apiPathPrefix configured API prefix used when matching paths
     */
    public void applyTo(final ApiRoutingDefinition routingDefinition, final String apiPathPrefix) {
        for (RoutingDefinition route : routingDefinition.definitions()) {
            ruleFor(route.verb(), route.url(), apiPathPrefix)
                    .ifPresent(rule -> rule.applyTo(route));
            applyEntityDefaultsTo(route, routingDefinition);
        }
        routingDefinition.updateOptionsAllowHeaders();
    }

    /**
     * Reports whether a route is disabled for a string verb.
     *
     * @param verb routing verb name
     * @param path request or generated route path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return true when a matching route rule disables the route
     */
    public boolean isDisabled(final String verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isDisabled)
                .orElse(false);
    }

    /**
     * Reports whether a route is disabled for a routing verb.
     *
     * @param verb routing verb
     * @param path request or generated route path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return true when a matching route rule disables the route
     */
    public boolean isDisabled(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isDisabled)
                .orElse(false);
    }

    /**
     * Reports whether a route should return 405 Method Not Allowed.
     *
     * @param verb routing verb
     * @param path request or generated route path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return true when a matching route rule marks the method unavailable
     */
    public boolean isMethodNotAllowed(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isMethodNotAllowed)
                .orElse(false);
    }

    /**
     * Finds the first route rule matching a string verb and path.
     *
     * @param verb routing verb name
     * @param path request or generated route path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return matching route rule when configured
     */
    public Optional<ThingifierApiRouteRule> ruleFor(
            final String verb, final String path, final String apiPathPrefix) {
        return ruleFor(RoutingVerb.valueOf(verb.trim().toUpperCase()), path, apiPathPrefix);
    }

    /**
     * Finds the first route rule matching a routing verb and path.
     *
     * <p>Path matching uses the same generated-route parameter behavior as runtime routing so
     * callers can configure rules using either concrete request paths or parameterized patterns.
     *
     * @param verb routing verb
     * @param path request or generated route path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return matching route rule when configured
     */
    public Optional<ThingifierApiRouteRule> ruleFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return routeRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .findFirst();
    }

    /**
     * Finds the authenticator registered for a named security scheme.
     *
     * @param schemeName named security scheme
     * @return authenticator when configured
     */
    public Optional<ThingifierApiAuthenticator> authenticatorFor(final String schemeName) {
        return Optional.ofNullable(
                authenticators.get(SecuritySchemeNames.requireValid(schemeName)));
    }

    /**
     * Resolves the request entity view for a route and entity.
     *
     * <p>Route-specific request views take precedence over entity defaults. Returning empty means
     * input should use the normal generated Thingifier validation rules.
     *
     * @param verb routing verb
     * @param path request path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @param entity entity receiving request fields
     * @return configured request entity view name when one applies
     */
    public Optional<String> requestEntityViewFor(
            final RoutingVerb verb,
            final String path,
            final String apiPathPrefix,
            final EntityDefinition entity) {
        final Optional<String> routeView =
                ruleFor(verb, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasRequestEntityView)
                        .map(ThingifierApiRouteRule::getRequestEntityView);
        if (routeView.isPresent()) {
            return routeView;
        }
        return defaultRequestEntityViewFor(entity);
    }

    /**
     * Resolves the response entity view for a route, entity, and status code.
     *
     * <p>Route status-specific views take precedence over entity defaults. Entity defaults only
     * apply to successful responses so error payloads keep their error shape.
     *
     * @param verb routing verb
     * @param path request path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @param entity entity being rendered
     * @param statusCode response status code
     * @return configured response entity view name when one applies
     */
    public Optional<String> responseEntityViewFor(
            final RoutingVerb verb,
            final String path,
            final String apiPathPrefix,
            final EntityDefinition entity,
            final int statusCode) {
        final Optional<String> routeView =
                ruleFor(verb, path, apiPathPrefix)
                        .map(rule -> rule.responseEntityViewFor(statusCode))
                        .filter(Objects::nonNull);
        if (routeView.isPresent()) {
            return routeView;
        }
        if (statusCode < 200 || statusCode >= 300) {
            return Optional.empty();
        }
        return defaultResponseEntityViewFor(entity);
    }

    /**
     * Finds the entity-level default request view for an entity.
     *
     * @param entity entity receiving request fields
     * @return default request view name when configured
     */
    public Optional<String> defaultRequestEntityViewFor(final EntityDefinition entity) {
        return entityRules.stream()
                .filter(rule -> rule.matches(entity))
                .filter(ThingifierApiEntityRule::hasDefaultRequestView)
                .map(ThingifierApiEntityRule::defaultRequestView)
                .findFirst();
    }

    /**
     * Finds the entity-level default response view for an entity.
     *
     * @param entity entity being rendered
     * @return default response view name when configured
     */
    public Optional<String> defaultResponseEntityViewFor(final EntityDefinition entity) {
        return entityRules.stream()
                .filter(rule -> rule.matches(entity))
                .filter(ThingifierApiEntityRule::hasDefaultResponseView)
                .map(ThingifierApiEntityRule::defaultResponseView)
                .findFirst();
    }

    /**
     * Returns an exact verb/path route rule, creating it when it does not already exist.
     *
     * <p>Path-level builders use this so declarations such as method-not-allowed compose with any
     * existing one-verb configuration for the same path.
     *
     * @param verb generated routing verb
     * @param pathPattern generated route path pattern
     * @return existing or newly-created route rule
     */
    ThingifierApiRouteRule routeFor(final RoutingVerb verb, final String pathPattern) {
        return routeRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(rule -> samePathPattern(rule.pathPattern(), pathPattern))
                .findFirst()
                .orElseGet(() -> route(verb, pathPattern));
    }

    /**
     * Resolves entity write operation policy for a route.
     *
     * <p>One-route rules take precedence over path-level entity policies.
     *
     * @param verb routing verb
     * @param path request path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return allowed operations when the spec overrides generated defaults
     */
    public Optional<Set<EntityWriteOperation>> entityWriteOperationsFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(verb, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasEntityWriteOperations);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().entityWriteOperations());
        }

        return entityWritePolicyRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(EntityWritePolicyRule::operations)
                .findFirst();
    }

    /**
     * Resolves PATCH update style policy for an entity route.
     *
     * @param path request path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return accepted PATCH styles when the spec overrides generated defaults
     */
    public Optional<Set<EntityPatchUpdateStyle>> entityPatchUpdateStylesFor(
            final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(RoutingVerb.PATCH, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasEntityPatchUpdateStyles);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().entityPatchUpdateStyles());
        }

        return entityPatchPolicyRules.stream()
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(EntityPatchPolicyRule::updateStyles)
                .findFirst();
    }

    /**
     * Resolves relationship write operation policy for a route.
     *
     * <p>One-route rules take precedence over path-level relationship policies.
     *
     * @param verb routing verb
     * @param path request path
     * @param apiPathPrefix configured API prefix used when matching paths
     * @return allowed operations when the spec overrides generated defaults
     */
    public Optional<Set<RelationshipWriteOperation>> relationshipWriteOperationsFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(verb, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasRelationshipWriteOperations);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().relationshipWriteOperations());
        }

        return relationshipWritePolicyRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(RelationshipWritePolicyRule::operations)
                .findFirst();
    }

    /**
     * Adds hide or disable rules for the standard collection and instance routes of an entity.
     *
     * @param entityPath entity path segment
     * @param disable true to disable routes, false to only hide them from docs
     */
    private void configureEntityRoutes(final String entityPath, final boolean disable) {
        final String collectionPath = "/" + normalize(entityPath);
        final String instancePath = collectionPath + "/{id}";
        for (RoutingVerb verb : RoutingVerb.values()) {
            configureRoute(verb, collectionPath, disable);
            configureRoute(verb, instancePath, disable);
        }
    }

    /**
     * Adds hide or disable rules for the standard relationship collection and instance routes.
     *
     * @param parentEntityPath parent entity path segment
     * @param relationshipName generated relationship route segment
     * @param disable true to disable routes, false to only hide them from docs
     */
    private void configureRelationshipRoutes(
            final String parentEntityPath, final String relationshipName, final boolean disable) {
        final String relationshipPath =
                "/" + normalize(parentEntityPath) + "/{id}/" + normalize(relationshipName);
        final String relationshipInstancePath = relationshipPath + "/{relatedId}";
        for (RoutingVerb verb : RoutingVerb.values()) {
            configureRoute(verb, relationshipPath, disable);
            configureRoute(verb, relationshipInstancePath, disable);
        }
    }

    /**
     * Creates a verb/path rule and applies either disabled or hidden state.
     *
     * @param verb routing verb to configure
     * @param pathPattern generated route path pattern
     * @param disable true to disable the route, false to only hide it from docs
     */
    private void configureRoute(
            final RoutingVerb verb, final String pathPattern, final boolean disable) {
        final ThingifierApiRouteRule rule = route(verb, pathPattern);
        if (disable) {
            rule.disable();
        } else {
            rule.hide();
        }
    }

    /**
     * Records entity write operation policy for generated collection and instance paths.
     *
     * @param verb POST or PUT
     * @param entityPath entity path segment
     * @param operations allowed write operations
     */
    private void configureEntityWritePolicy(
            final RoutingVerb verb,
            final String entityPath,
            final EntityWriteOperation... operations) {
        final String collectionPath = "/" + normalize(entityPath);
        final String instancePath = collectionPath + "/{id}";
        if (verb == RoutingVerb.POST || verb == RoutingVerb.PUT) {
            entityWritePolicyRules.add(
                    new EntityWritePolicyRule(verb, collectionPath, entityOperations(operations)));
        }
        entityWritePolicyRules.add(
                new EntityWritePolicyRule(verb, instancePath, entityOperations(operations)));
    }

    /**
     * Records PATCH update style policy for generated instance paths.
     *
     * @param entityPath entity path segment
     * @param updateStyles accepted PATCH styles
     */
    private void configureEntityPatchPolicy(
            final String entityPath, final EntityPatchUpdateStyle... updateStyles) {
        final String instancePath = "/" + normalize(entityPath) + "/{id}";
        entityPatchPolicyRules.add(
                new EntityPatchPolicyRule(instancePath, entityPatchStyles(updateStyles)));
    }

    /**
     * Records relationship write operation policy for generated relationship paths.
     *
     * @param verb POST or DELETE
     * @param parentEntityPath parent entity path segment
     * @param relationshipName generated relationship route segment
     * @param operations allowed relationship operations
     */
    private void configureRelationshipWritePolicy(
            final RoutingVerb verb,
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        final String relationshipPath =
                "/" + normalize(parentEntityPath) + "/{id}/" + normalize(relationshipName);
        final String path =
                verb == RoutingVerb.DELETE ? relationshipPath + "/{relatedId}" : relationshipPath;
        relationshipWritePolicyRules.add(
                new RelationshipWritePolicyRule(verb, path, relationshipOperations(operations)));
    }

    /**
     * Applies entity-level default views to generated route payload metadata.
     *
     * <p>Route-level request or response views remain unchanged so explicit endpoint contracts keep
     * precedence over entity defaults.
     *
     * @param route generated route metadata to update
     * @param routingDefinition route definition set containing payload schemas
     */
    private void applyEntityDefaultsTo(
            final RoutingDefinition route, final ApiRoutingDefinition routingDefinition) {
        if (!route.hasRequestEntityView() && route.hasRequestPayload()) {
            routingDefinition
                    .objectSchemaNamed(route.getRequestPayload())
                    .flatMap(this::defaultRequestEntityViewFor)
                    .ifPresent(route::requestEntityView);
        }

        for (Integer statusCode : route.returnPayloadStatusCodes()) {
            if (route.hasResponseEntityViewFor(statusCode)) {
                continue;
            }
            if (statusCode < 200 || statusCode >= 300) {
                continue;
            }
            routingDefinition
                    .objectSchemaNamed(route.getReturnPayloadFor(statusCode))
                    .flatMap(this::defaultResponseEntityViewFor)
                    .ifPresent(viewName -> route.responseEntityView(statusCode, viewName));
        }
    }

    /**
     * Converts caller supplied entity write operations into an immutable policy set.
     *
     * @param operations configured write operations, possibly null
     * @return immutable operation set, or an empty set when no operations are configured
     */
    private Set<EntityWriteOperation> entityOperations(final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    /**
     * Converts caller supplied PATCH styles into an immutable policy set.
     *
     * @param updateStyles configured PATCH styles, possibly null
     * @return immutable style set, or an empty set when no styles are configured
     */
    private Set<EntityPatchUpdateStyle> entityPatchStyles(
            final EntityPatchUpdateStyle... updateStyles) {
        EnumSet<EntityPatchUpdateStyle> selected = EnumSet.noneOf(EntityPatchUpdateStyle.class);
        if (updateStyles != null) {
            Collections.addAll(selected, updateStyles);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    /**
     * Converts caller supplied relationship operations into an immutable policy set.
     *
     * @param operations configured relationship operations, possibly null
     * @return immutable operation set, or an empty set when no operations are configured
     */
    private Set<RelationshipWriteOperation> relationshipOperations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    /**
     * Normalizes path segments so equivalent API spec inputs match generated route patterns.
     *
     * @param path path or path segment, with optional leading/trailing slashes
     * @return path without leading or trailing slash characters
     */
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

    /**
     * Compares generated path patterns using the same slash-tolerant normalization.
     *
     * @param first first path pattern
     * @param second second path pattern
     * @return true when the patterns are equivalent after normalization
     */
    private boolean samePathPattern(final String first, final String second) {
        return normalize(first).equals(normalize(second));
    }

    private static final class EntityWritePolicyRule {
        private final RoutingVerb verb;
        private final String pathPattern;
        private final Set<EntityWriteOperation> operations;

        EntityWritePolicyRule(
                final RoutingVerb verb,
                final String pathPattern,
                final Set<EntityWriteOperation> operations) {
            this.verb = verb;
            this.pathPattern = pathPattern;
            this.operations = operations;
        }

        RoutingVerb verb() {
            return verb;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<EntityWriteOperation> operations() {
            return operations;
        }
    }

    private static final class EntityPatchPolicyRule {
        private final String pathPattern;
        private final Set<EntityPatchUpdateStyle> updateStyles;

        EntityPatchPolicyRule(
                final String pathPattern, final Set<EntityPatchUpdateStyle> updateStyles) {
            this.pathPattern = pathPattern;
            this.updateStyles = updateStyles;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<EntityPatchUpdateStyle> updateStyles() {
            return updateStyles;
        }
    }

    private static final class RelationshipWritePolicyRule {
        private final RoutingVerb verb;
        private final String pathPattern;
        private final Set<RelationshipWriteOperation> operations;

        RelationshipWritePolicyRule(
                final RoutingVerb verb,
                final String pathPattern,
                final Set<RelationshipWriteOperation> operations) {
            this.verb = verb;
            this.pathPattern = pathPattern;
            this.operations = operations;
        }

        RoutingVerb verb() {
            return verb;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<RelationshipWriteOperation> operations() {
            return operations;
        }
    }
}
