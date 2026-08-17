package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
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
    private boolean usesBearerAuth;
    private String documentation;
    private String requestPayload;
    private String requestEntityView;
    private String defaultEntityView;
    private Map<Integer, String> responseEntityViews;
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
        this.usesBearerAuth = false;
        this.documentation = null;
        this.requestPayload = null;
        this.requestEntityView = null;
        this.defaultEntityView = null;
        this.responseEntityViews = new HashMap<>();
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
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBasicAuth() {
        usesBasicAuth = true;
        return this;
    }

    /**
     * Marks the route as requiring Bearer authentication in generated documentation.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBearerAuth() {
        usesBearerAuth = true;
        return this;
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
            route.secureWithBasicAuth();
        }
        if (usesBearerAuth) {
            route.secureWithBearerAuth();
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
}
