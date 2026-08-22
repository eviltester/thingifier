package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.security.SecuritySchemeNames;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizer;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;

/**
 * Configures the generated API behaviour for one verb and path pattern.
 *
 * <p>Route rules are deliberately narrow overrides for generated Thingifier routes. They let a
 * model keep its generated route set while shaping the public API with documentation, visibility,
 * authentication, allowed write semantics, and entity views.
 */
public final class ThingifierApiRouteRule {

    private final RoutingVerb verb;
    private final String pathPattern;
    private boolean hidden;
    private boolean disabled;
    private boolean methodNotAllowed;
    private boolean usesBasicAuth;
    private String basicAuthSchemeName;
    private String enforcedBasicAuthSchemeName;
    private boolean usesBearerAuth;
    private String bearerAuthSchemeName;
    private String enforcedBearerAuthSchemeName;
    private final List<ThingifierApiAuthorizer> authorizers;
    private String documentation;
    private String requestPayload;
    private String requestEntityView;
    private String defaultEntityView;
    private Map<Integer, String> responseEntityViews;
    private String mappedEntityName;
    private String fixedIdentifier;
    private FixedResourcePolicy fixedResourcePolicy;
    private EnumSet<EntityWriteOperation> entityWriteOperations;
    private EnumSet<EntityPatchUpdateStyle> entityPatchUpdateStyles;
    private EnumSet<RelationshipWriteOperation> relationshipWriteOperations;

    ThingifierApiRouteRule(final RoutingVerb verb, final String pathPattern) {
        this.verb = verb;
        this.pathPattern = pathPattern == null ? "" : pathPattern;
        this.hidden = false;
        this.disabled = false;
        this.methodNotAllowed = false;
        this.usesBasicAuth = false;
        this.basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
        this.enforcedBasicAuthSchemeName = null;
        this.usesBearerAuth = false;
        this.bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
        this.enforcedBearerAuthSchemeName = null;
        this.authorizers = new java.util.ArrayList<>();
        this.documentation = null;
        this.requestPayload = null;
        this.requestEntityView = null;
        this.defaultEntityView = null;
        this.responseEntityViews = new HashMap<>();
        this.mappedEntityName = null;
        this.fixedIdentifier = null;
        this.fixedResourcePolicy = FixedResourcePolicy.RETURN_404;
        this.entityWriteOperations = null;
        this.entityPatchUpdateStyles = null;
        this.relationshipWriteOperations = null;
    }

    /**
     * Returns the HTTP-style verb this rule applies to.
     *
     * @return the configured routing verb
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * Returns the route path pattern this rule applies to.
     *
     * <p>Patterns use the same parameter forms as generated routes, including {@code {id}} and
     * {@code :id}.
     *
     * @return configured route path pattern
     */
    public String pathPattern() {
        return pathPattern;
    }

    /**
     * Hides the generated route from API documentation while leaving runtime behaviour available.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule hide() {
        hidden = true;
        return this;
    }

    /**
     * Hides the generated route from API documentation.
     *
     * <p>This alias keeps older configuration readable while making the intent explicit.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule hideFromDocs() {
        return hide();
    }

    /**
     * Disables the generated route so it behaves like it is not part of the API.
     *
     * <p>Disabled routes are hidden from documentation and return the existing not-found style
     * response instead of a 405.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule disable() {
        disabled = true;
        return this;
    }

    /**
     * Marks the generated route as present but unavailable for this method.
     *
     * <p>This is for public API surface decisions where clients should receive HTTP 405 Method Not
     * Allowed and an Allow header rather than a not-found response.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule methodNotAllowed() {
        methodNotAllowed = true;
        return this;
    }

    /**
     * Reports whether this route should be hidden from documentation.
     *
     * @return true when the route is documentation-hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Reports whether this route should behave as disabled.
     *
     * @return true when the route should be treated as absent at runtime
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Reports whether this route should return 405 Method Not Allowed.
     *
     * @return true when the method is publicly unavailable but still a known route
     */
    public boolean isMethodNotAllowed() {
        return methodNotAllowed;
    }

    /**
     * Marks the route as requiring Basic authentication in generated documentation.
     *
     * <p>This historical form is documentation-only. Use {@link #secureWithBasicAuth(String)} when
     * Thingifier should also enforce a named Basic policy at runtime.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBasicAuth() {
        usesBasicAuth = true;
        basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
        return this;
    }

    /**
     * Marks the route as requiring a named Basic authentication scheme and runtime enforcement.
     *
     * <p>The scheme name is used in generated documentation, authenticator lookup, and the
     * authenticated-principal slot on the request context. The Basic realm is configured on {@link
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec#basic(String, String)}.
     * If both named Basic and named Bearer auth are configured on one route, the most recent named
     * call selects the runtime enforcement scheme.
     *
     * @param schemeName named Basic scheme, for example {@code adminPassword}
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBasicAuth(final String schemeName) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        usesBasicAuth = true;
        usesBearerAuth = false;
        basicAuthSchemeName = normalizedSchemeName;
        enforcedBasicAuthSchemeName = normalizedSchemeName;
        enforcedBearerAuthSchemeName = null;
        return this;
    }

    /**
     * Marks the route as requiring Bearer authentication in generated documentation.
     *
     * <p>This historical form is documentation-only. Use {@link #secureWithBearerAuth(String)} when
     * Thingifier should also enforce a named bearer policy at runtime.
     *
     * @return this rule so route API configuration can be chained
     */
    @Deprecated
    public ThingifierApiRouteRule secureWithBearerAuth() {
        usesBearerAuth = true;
        bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
        return this;
    }

    /**
     * Marks the route as requiring a named Bearer authentication scheme and runtime enforcement.
     *
     * <p>The scheme name is used in generated documentation, authenticator lookup, and the
     * authenticated-principal slot on the request context. Applications supply the authenticator
     * through {@link ThingifierApiSpec#authenticator(String,
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticator)}.
     *
     * @param schemeName named bearer scheme, for example {@code cartToken}
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBearerAuth(final String schemeName) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        usesBasicAuth = false;
        usesBearerAuth = true;
        bearerAuthSchemeName = normalizedSchemeName;
        enforcedBasicAuthSchemeName = null;
        enforcedBearerAuthSchemeName = normalizedSchemeName;
        return this;
    }

    /**
     * Adds a route-specific authorization callback.
     *
     * <p>Authorizers run only after the named authenticator accepts the bearer token. Multiple
     * authorizers are evaluated in registration order and the first rejection stops the request.
     *
     * @param authorizer authorization callback
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the authorizer is null
     */
    public ThingifierApiRouteRule authorizeWith(final ThingifierApiAuthorizer authorizer) {
        if (authorizer == null) {
            throw new IllegalArgumentException("authorizer is required");
        }
        authorizers.add(authorizer);
        return this;
    }

    /**
     * Reports whether this route is documented as bearer secured.
     *
     * @return true when bearer auth should appear in generated documentation
     */
    public boolean isSecuredByBearerAuth() {
        return usesBearerAuth;
    }

    /**
     * Returns the bearer scheme name used in generated documentation.
     *
     * @return bearer auth scheme name
     */
    public String bearerAuthSchemeName() {
        return bearerAuthSchemeName;
    }

    /**
     * Reports whether this route should enforce bearer auth at runtime.
     *
     * @return true when the route has a named bearer enforcement scheme
     */
    public boolean hasBearerAuthEnforcement() {
        return enforcedBearerAuthSchemeName != null;
    }

    /**
     * Returns the bearer scheme name used for runtime enforcement.
     *
     * @return bearer enforcement scheme name, or null for documentation-only bearer routes
     */
    public String bearerAuthEnforcementSchemeName() {
        return enforcedBearerAuthSchemeName;
    }

    /**
     * Returns route-specific authorizers in registration order.
     *
     * @return immutable authorizer list
     */
    public List<ThingifierApiAuthorizer> authorizers() {
        return List.copyOf(authorizers);
    }

    /**
     * Reports whether this route is documented as Basic secured.
     *
     * @return true when Basic auth should appear in generated documentation
     */
    public boolean isSecuredByBasicAuth() {
        return usesBasicAuth;
    }

    /**
     * Returns the Basic scheme name used in generated documentation.
     *
     * @return Basic auth scheme name
     */
    public String basicAuthSchemeName() {
        return basicAuthSchemeName;
    }

    /**
     * Reports whether this route should enforce Basic auth at runtime.
     *
     * @return true when the route has a named Basic enforcement scheme
     */
    public boolean hasBasicAuthEnforcement() {
        return enforcedBasicAuthSchemeName != null;
    }

    /**
     * Returns the Basic scheme name used for runtime enforcement.
     *
     * @return Basic enforcement scheme name, or null for documentation-only Basic routes
     */
    public String basicAuthEnforcementSchemeName() {
        return enforcedBasicAuthSchemeName;
    }

    /**
     * Adds route-specific documentation text to the generated API definition.
     *
     * @param documentation documentation text to append to the route definition
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule addDocumentation(final String documentation) {
        this.documentation = documentation;
        return this;
    }

    /**
     * Overrides the named request payload schema advertised for this route.
     *
     * @param requestPayload object schema name to use as the request payload
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule requestPayload(final String requestPayload) {
        this.requestPayload = requestPayload;
        return this;
    }

    /**
     * Sets the entity view used to validate accepted request fields for this route.
     *
     * <p>Route-specific request views override entity-level defaults because endpoint contracts can
     * be narrower or broader than the normal entity contract.
     *
     * @param viewName entity view name used for request validation
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule requestEntityView(final String viewName) {
        this.requestEntityView = viewName;
        return this;
    }

    /**
     * Sets the response entity view for a specific successful or error status code.
     *
     * @param statusCode status code whose response payload should use the view
     * @param viewName entity view name used when rendering the response body
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule responseEntityView(final int statusCode, final String viewName) {
        this.responseEntityViews.put(statusCode, viewName);
        return this;
    }

    /**
     * Sets the default response entity view for successful responses on this route.
     *
     * <p>This is the response-only counterpart to {@link #entityView(String)}. It is useful for
     * fixed instance routes where the public response shape should be constrained but writes may
     * still accept a different request view.
     *
     * @param viewName entity view name used for successful responses
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule defaultEntityView(final String viewName) {
        this.defaultEntityView = viewName;
        return this;
    }

    /**
     * Declares the model entity that a non-generated public route should target.
     *
     * <p>Use this with {@link #withFixedIdentifier(String)} when a route such as {@code
     * /secret/note} should be processed as a Thingifier-managed entity instance without putting the
     * identifier in the public URL.
     *
     * @param entityName singular or plural model entity name
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the entity name is blank
     */
    public ThingifierApiRouteRule mapsToEntity(final String entityName) {
        this.mappedEntityName = requireText(entityName, "entity name");
        return this;
    }

    /**
     * Maps this public route to one fixed entity identifier.
     *
     * <p>The fixed identifier is a trusted server-side route decision, not a value read from the
     * client URL. Runtime processing resolves the public route to an internal instance route while
     * keeping the public path available for documentation, hooks, and error messages.
     *
     * @param identifier identifier of the target entity instance
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the identifier is blank or the route path has URL
     *     parameters
     */
    public ThingifierApiRouteRule withFixedIdentifier(final String identifier) {
        return withFixedIdentifier(identifier, FixedResourcePolicy.RETURN_404);
    }

    /**
     * Maps this public route to one fixed entity identifier with explicit missing-instance policy.
     *
     * @param identifier identifier of the target entity instance
     * @param policy behaviour when the target instance is missing
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the identifier is blank, the policy is null, or the
     *     route path has URL parameters
     */
    public ThingifierApiRouteRule withFixedIdentifier(
            final String identifier, final FixedResourcePolicy policy) {
        if (hasPathParameter(pathPattern)) {
            throw new IllegalArgumentException(
                    "fixed identifier routes must not contain path parameters");
        }
        this.fixedIdentifier = requireText(identifier, "fixed identifier");
        if (policy == null) {
            throw new IllegalArgumentException("fixed resource policy is required");
        }
        this.fixedResourcePolicy = policy;
        return this;
    }

    /**
     * Restricts which entity write operations this generated route may perform.
     *
     * <p>The route may still be generated; the runtime chooses 405 when the concrete write
     * operation is outside this set.
     *
     * @param operations allowed create or update operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityWriteOperations(final EntityWriteOperation... operations) {
        this.entityWriteOperations = entityOperations(operations);
        return this;
    }

    /**
     * Alias for {@link #entityWriteOperations(EntityWriteOperation...)}.
     *
     * @param operations allowed create or update operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityCan(final EntityWriteOperation... operations) {
        return entityWriteOperations(operations);
    }

    /**
     * Restricts which patch document styles this generated entity route accepts.
     *
     * @param updateStyles accepted patch formats for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityPatchCan(final EntityPatchUpdateStyle... updateStyles) {
        this.entityPatchUpdateStyles = patchStyles(updateStyles);
        return this;
    }

    /**
     * Restricts which relationship write operations this generated route may perform.
     *
     * @param operations allowed relationship operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule relationshipWriteOperations(
            final RelationshipWriteOperation... operations) {
        this.relationshipWriteOperations = relationshipOperations(operations);
        return this;
    }

    /**
     * Alias for {@link #relationshipWriteOperations(RelationshipWriteOperation...)}.
     *
     * @param operations allowed relationship operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule relationshipCan(final RelationshipWriteOperation... operations) {
        return relationshipWriteOperations(operations);
    }

    /**
     * Uses the same entity view for request validation and successful responses on this route.
     *
     * <p>This route-level view overrides entity defaults and is kept for concise route contracts.
     *
     * @param viewName entity view name for route input and output
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityView(final String viewName) {
        this.requestEntityView = viewName;
        this.defaultEntityView = viewName;
        return this;
    }

    /**
     * Reports whether this route has an explicit request entity view.
     *
     * @return true when request input should use the configured route view
     */
    public boolean hasRequestEntityView() {
        return requestEntityView != null;
    }

    /**
     * Returns the explicit request entity view configured for this route.
     *
     * @return request entity view name, or null when the route has none
     */
    public String getRequestEntityView() {
        return requestEntityView;
    }

    /**
     * Resolves the response entity view for the supplied status code.
     *
     * <p>Status-specific views win first. The route default applies only to 2xx responses so error
     * bodies are not accidentally shaped like normal entity resources.
     *
     * @param statusCode API response status code
     * @return entity view name, or null when this route does not define one for the status
     */
    public String responseEntityViewFor(final int statusCode) {
        if (responseEntityViews.containsKey(statusCode)) {
            return responseEntityViews.get(statusCode);
        }
        if (defaultEntityView != null && statusCode >= 200 && statusCode < 300) {
            return defaultEntityView;
        }
        return null;
    }

    /**
     * Reports whether this route overrides entity write operation policy.
     *
     * @return true when entity write operations were explicitly configured
     */
    public boolean hasEntityWriteOperations() {
        return entityWriteOperations != null;
    }

    /**
     * Returns the entity write operations allowed by this route.
     *
     * @return immutable set of allowed entity write operations, or an empty set when none are
     *     allowed
     */
    public Set<EntityWriteOperation> entityWriteOperations() {
        if (entityWriteOperations == null || entityWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(entityWriteOperations));
    }

    /**
     * Reports whether this route overrides PATCH update style policy.
     *
     * @return true when PATCH styles were explicitly configured
     */
    public boolean hasEntityPatchUpdateStyles() {
        return entityPatchUpdateStyles != null;
    }

    /**
     * Returns the PATCH update styles allowed by this route.
     *
     * @return immutable set of accepted PATCH styles, or an empty set when none are allowed
     */
    public Set<EntityPatchUpdateStyle> entityPatchUpdateStyles() {
        if (entityPatchUpdateStyles == null || entityPatchUpdateStyles.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(entityPatchUpdateStyles));
    }

    /**
     * Reports whether this route overrides relationship write operation policy.
     *
     * @return true when relationship write operations were explicitly configured
     */
    public boolean hasRelationshipWriteOperations() {
        return relationshipWriteOperations != null;
    }

    /**
     * Returns the relationship write operations allowed by this route.
     *
     * @return immutable set of allowed relationship write operations, or an empty set when none are
     *     allowed
     */
    public Set<RelationshipWriteOperation> relationshipWriteOperations() {
        if (relationshipWriteOperations == null || relationshipWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(relationshipWriteOperations));
    }

    /**
     * Reports whether this rule maps a public path to a fixed entity instance.
     *
     * @return true when both entity and identifier are configured
     */
    public boolean hasFixedIdentifierMapping() {
        return mappedEntityName != null && fixedIdentifier != null;
    }

    /**
     * Returns the entity name configured for a fixed identifier route.
     *
     * @return singular or plural entity name, or null when not configured
     */
    public String fixedEntityName() {
        return mappedEntityName;
    }

    /**
     * Returns the fixed identifier configured for this route.
     *
     * @return fixed entity instance identifier, or null when not configured
     */
    public String fixedIdentifier() {
        return fixedIdentifier;
    }

    /**
     * Returns the configured missing-instance policy for this fixed route.
     *
     * @return fixed resource policy, defaulting to {@link FixedResourcePolicy#RETURN_404}
     */
    public FixedResourcePolicy fixedResourcePolicy() {
        return fixedResourcePolicy;
    }

    /**
     * Applies this API-spec rule to a generated route definition.
     *
     * <p>Documentation route metadata is updated here, while runtime policy is resolved separately
     * by the HTTP and direct API handlers so both execution paths remain consistent.
     *
     * @param route generated routing definition to update
     */
    void applyTo(final RoutingDefinition route) {
        if (hidden) {
            route.hideFromDocumentation();
        }
        if (disabled) {
            route.disable();
        }
        if (methodNotAllowed && !disabled) {
            route.replaceStatus(RoutingStatus.returnValue(405));
        }
        if (usesBasicAuth) {
            route.secureWithBasicAuth(basicAuthSchemeName);
        }
        if (usesBearerAuth) {
            route.secureWithBearerAuth(bearerAuthSchemeName);
        }
        if (documentation != null) {
            route.addDocumentation(documentation);
        }
        if (requestPayload != null) {
            route.requestPayload(requestPayload);
        }
        if (requestEntityView != null) {
            route.requestEntityView(requestEntityView);
        }
        for (Map.Entry<Integer, String> responseView : responseEntityViews.entrySet()) {
            route.responseEntityView(responseView.getKey(), responseView.getValue());
        }
        if (defaultEntityView != null) {
            if (route.returnPayloadStatusCodes().isEmpty()) {
                route.responseEntityView(200, defaultEntityView);
                route.responseEntityView(201, defaultEntityView);
            } else {
                for (Integer statusCode : route.returnPayloadStatusCodes()) {
                    if (statusCode >= 200
                            && statusCode < 300
                            && !route.hasResponseEntityViewFor(statusCode)) {
                        route.responseEntityView(statusCode, defaultEntityView);
                    }
                }
            }
        }
        if (hasFixedIdentifierMapping()) {
            route.mapToFixedEntity(mappedEntityName, fixedIdentifier, fixedResourcePolicy);
        }
    }

    /**
     * Captures the configured entity write operations in an enum set.
     *
     * @param operations caller supplied operations, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<EntityWriteOperation> entityOperations(
            final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    /**
     * Captures the configured patch styles in an enum set.
     *
     * @param styles caller supplied patch styles, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<EntityPatchUpdateStyle> patchStyles(final EntityPatchUpdateStyle... styles) {
        EnumSet<EntityPatchUpdateStyle> selected = EnumSet.noneOf(EntityPatchUpdateStyle.class);
        if (styles != null) {
            Collections.addAll(selected, styles);
        }
        return selected;
    }

    /**
     * Captures the configured relationship write operations in an enum set.
     *
     * @param operations caller supplied operations, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<RelationshipWriteOperation> relationshipOperations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    private String requireText(final String value, final String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }

    private boolean hasPathParameter(final String path) {
        final String normalized = path == null ? "" : path.trim();
        if (normalized.contains("{") || normalized.contains("}")) {
            return true;
        }
        for (String segment : normalized.split("/")) {
            if (segment.startsWith(":") && segment.length() > 1) {
                return true;
            }
        }
        return false;
    }
}
