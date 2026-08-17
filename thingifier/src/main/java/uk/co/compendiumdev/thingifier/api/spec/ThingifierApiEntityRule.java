package uk.co.compendiumdev.thingifier.api.spec;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Configures API-wide defaults for one Thingifier entity.
 *
 * <p>Entity rules exist so a generated API can describe a stable public shape once for an entity
 * instead of repeating the same request and response view settings on every generated route.
 * Route-level configuration still has higher precedence when a particular endpoint needs a
 * different representation.
 */
public final class ThingifierApiEntityRule {

    private final String entityName;
    private String defaultRequestView;
    private String defaultResponseView;

    ThingifierApiEntityRule(final String entityName) {
        this.entityName = requiredName("Entity name", entityName);
        this.defaultRequestView = null;
        this.defaultResponseView = null;
    }

    /**
     * Returns the entity name this rule was configured for.
     *
     * <p>The name may be singular or plural; matching is intentionally case-insensitive so callers
     * can configure rules using the names they already use in route paths.
     *
     * @return the normalized rule target as supplied by the caller, after trimming
     */
    public String entityName() {
        return entityName;
    }

    /**
     * Sets the default view used to validate write request bodies for this entity.
     *
     * <p>This lets the API reject fields which are part of the internal model but not accepted by
     * the public API. A route-specific request view overrides this default.
     *
     * @param viewName entity view name to apply to write input
     * @return this rule so entity API configuration can be chained
     */
    public ThingifierApiEntityRule defaultRequestView(final String viewName) {
        defaultRequestView = requiredName("View name", viewName);
        return this;
    }

    /**
     * Sets the default view used when rendering successful responses for this entity.
     *
     * <p>The default applies to collection, instance, relationship, direct API, and HTTP API
     * responses unless the matching route declares a response view for the status code.
     *
     * @param viewName entity view name to apply to successful response bodies
     * @return this rule so entity API configuration can be chained
     */
    public ThingifierApiEntityRule defaultResponseView(final String viewName) {
        defaultResponseView = requiredName("View name", viewName);
        return this;
    }

    /**
     * Sets the same default view for write validation and response rendering.
     *
     * <p>This is the compact form for entities where the external input and output surface are the
     * same. Use the request or response specific methods when the public API accepts and returns
     * different fields.
     *
     * @param viewName entity view name to use for both request and response defaults
     * @return this rule so entity API configuration can be chained
     */
    public ThingifierApiEntityRule defaultEntityView(final String viewName) {
        defaultRequestView(viewName);
        defaultResponseView(viewName);
        return this;
    }

    /**
     * Reports whether this entity has a request-body view default.
     *
     * @return true when write input should be checked against a configured entity view
     */
    public boolean hasDefaultRequestView() {
        return defaultRequestView != null;
    }

    /**
     * Returns the configured default request view.
     *
     * @return the request view name, or null when no request default has been configured
     */
    public String defaultRequestView() {
        return defaultRequestView;
    }

    /**
     * Reports whether this entity has a response-body view default.
     *
     * @return true when successful responses should be rendered through a configured entity view
     */
    public boolean hasDefaultResponseView() {
        return defaultResponseView != null;
    }

    /**
     * Returns the configured default response view.
     *
     * @return the response view name, or null when no response default has been configured
     */
    public String defaultResponseView() {
        return defaultResponseView;
    }

    /**
     * Matches this rule to an entity definition by singular or plural name.
     *
     * <p>The API spec accepts either spelling because callers often think in route paths, while the
     * model stores both names on the entity definition.
     *
     * @param entity entity definition being resolved for a route or response
     * @return true when the rule targets the supplied entity
     */
    boolean matches(final EntityDefinition entity) {
        if (entity == null) {
            return false;
        }
        return normalized(entityName).equals(normalized(entity.getName()))
                || normalized(entityName).equals(normalized(entity.getPlural()));
    }

    /**
     * Matches this rule to a caller-supplied entity name.
     *
     * <p>This is used when adding rules so repeated calls to {@code entity("todos")} update the
     * existing rule instead of creating competing defaults for the same entity.
     *
     * @param name singular or plural entity name to compare
     * @return true when the supplied name refers to the same configured entity
     */
    boolean sameEntityName(final String name) {
        return normalized(entityName).equals(normalized(name));
    }

    /**
     * Normalizes and validates a required API-spec name.
     *
     * @param label name used in the exception message
     * @param name candidate name from the caller
     * @return trimmed name when present
     */
    private String requiredName(final String label, final String name) {
        final String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return normalized;
    }

    /**
     * Converts optional API-spec names into the comparison form used by rule matching.
     *
     * @param name candidate name, possibly null
     * @return trimmed lower-case name, or an empty string for null
     */
    private String normalized(final String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }
}
