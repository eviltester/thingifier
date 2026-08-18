package uk.co.compendiumdev.thingifier.api.security;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Immutable route details shared by authentication and authorization contexts.
 *
 * <p>The internal auth policy builds this once per request so both callbacks see the same route,
 * path-parameter, entity, and store information.
 */
public final class ThingifierApiRouteAuthDetails {

    private final String schemeName;
    private final RoutingVerb verb;
    private final String path;
    private final Map<String, String> pathParameters;
    private final ThingRoute route;
    private final HttpHeadersBlock headers;
    private final ThingifierRequestContext requestContext;
    private final EntityDefinition targetEntity;
    private final String targetIdentifier;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    /**
     * Creates route auth details.
     *
     * @param builder populated details builder
     */
    private ThingifierApiRouteAuthDetails(final Builder builder) {
        this.schemeName = builder.schemeName;
        this.verb = builder.verb;
        this.path = builder.path;
        this.pathParameters = Map.copyOf(builder.pathParameters);
        this.route = builder.route;
        this.headers = builder.headers;
        this.requestContext = builder.requestContext;
        this.targetEntity = builder.targetEntity;
        this.targetIdentifier = builder.targetIdentifier;
        this.parentEntity = builder.parentEntity;
        this.parentIdentifier = builder.parentIdentifier;
        this.relationshipName = builder.relationshipName;
        this.childIdentifier = builder.childIdentifier;
    }

    /**
     * Starts a builder for route auth details.
     *
     * @return new details builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the security scheme name.
     *
     * @return security scheme name
     */
    public String schemeName() {
        return schemeName;
    }

    /**
     * Returns the generated API verb.
     *
     * @return routing verb
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * Returns the generated API path.
     *
     * @return request path
     */
    public String path() {
        return path;
    }

    /**
     * Returns named path parameters.
     *
     * @return immutable path-parameter map
     */
    public Map<String, String> pathParameters() {
        return pathParameters;
    }

    /**
     * Returns the mapped generated route.
     *
     * @return route abstraction
     */
    public ThingRoute route() {
        return route;
    }

    /**
     * Returns request headers.
     *
     * @return request header block
     */
    public HttpHeadersBlock headers() {
        return headers;
    }

    /**
     * Returns the active request context.
     *
     * @return request context
     */
    public ThingifierRequestContext requestContext() {
        return requestContext;
    }

    /**
     * Returns the target entity.
     *
     * @return target entity, or null
     */
    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    /**
     * Returns the target identifier.
     *
     * @return target identifier, or null
     */
    public String targetIdentifier() {
        return targetIdentifier;
    }

    /**
     * Returns the parent entity for relationship routes.
     *
     * @return parent entity, or null
     */
    public EntityDefinition parentEntity() {
        return parentEntity;
    }

    /**
     * Returns the parent identifier for relationship routes.
     *
     * @return parent identifier, or null
     */
    public String parentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Returns the relationship name.
     *
     * @return relationship name, or null
     */
    public String relationshipName() {
        return relationshipName;
    }

    /**
     * Returns the child identifier for relationship instance routes.
     *
     * @return child identifier, or null
     */
    public String childIdentifier() {
        return childIdentifier;
    }

    /** Mutable builder for {@link ThingifierApiRouteAuthDetails}. */
    public static final class Builder {
        private String schemeName;
        private RoutingVerb verb;
        private String path;
        private Map<String, String> pathParameters = Map.of();
        private ThingRoute route;
        private HttpHeadersBlock headers;
        private ThingifierRequestContext requestContext;
        private EntityDefinition targetEntity;
        private String targetIdentifier;
        private EntityDefinition parentEntity;
        private String parentIdentifier;
        private String relationshipName;
        private String childIdentifier;

        private Builder() {}

        /**
         * Sets the security scheme name.
         *
         * @param schemeName security scheme name
         * @return this builder
         */
        public Builder schemeName(final String schemeName) {
            this.schemeName = schemeName;
            return this;
        }

        /**
         * Sets the generated API verb.
         *
         * @param verb routing verb
         * @return this builder
         */
        public Builder verb(final RoutingVerb verb) {
            this.verb = verb;
            return this;
        }

        /**
         * Sets the generated API path.
         *
         * @param path request path
         * @return this builder
         */
        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets named path parameters.
         *
         * @param pathParameters path-parameter map
         * @return this builder
         */
        public Builder pathParameters(final Map<String, String> pathParameters) {
            this.pathParameters = pathParameters == null ? Map.of() : pathParameters;
            return this;
        }

        /**
         * Sets the mapped generated route.
         *
         * @param route route abstraction
         * @return this builder
         */
        public Builder route(final ThingRoute route) {
            this.route = route;
            return this;
        }

        /**
         * Sets request headers.
         *
         * @param headers request header block
         * @return this builder
         */
        public Builder headers(final HttpHeadersBlock headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Sets the active request context.
         *
         * @param requestContext request context
         * @return this builder
         */
        public Builder requestContext(final ThingifierRequestContext requestContext) {
            this.requestContext = requestContext;
            return this;
        }

        /**
         * Sets the target entity.
         *
         * @param targetEntity target entity
         * @return this builder
         */
        public Builder targetEntity(final EntityDefinition targetEntity) {
            this.targetEntity = targetEntity;
            return this;
        }

        /**
         * Sets the target identifier.
         *
         * @param targetIdentifier target identifier
         * @return this builder
         */
        public Builder targetIdentifier(final String targetIdentifier) {
            this.targetIdentifier = targetIdentifier;
            return this;
        }

        /**
         * Sets the parent entity for relationship routes.
         *
         * @param parentEntity parent entity
         * @return this builder
         */
        public Builder parentEntity(final EntityDefinition parentEntity) {
            this.parentEntity = parentEntity;
            return this;
        }

        /**
         * Sets the parent identifier for relationship routes.
         *
         * @param parentIdentifier parent identifier
         * @return this builder
         */
        public Builder parentIdentifier(final String parentIdentifier) {
            this.parentIdentifier = parentIdentifier;
            return this;
        }

        /**
         * Sets the relationship name.
         *
         * @param relationshipName relationship name
         * @return this builder
         */
        public Builder relationshipName(final String relationshipName) {
            this.relationshipName = relationshipName;
            return this;
        }

        /**
         * Sets the child identifier for relationship instance routes.
         *
         * @param childIdentifier child identifier
         * @return this builder
         */
        public Builder childIdentifier(final String childIdentifier) {
            this.childIdentifier = childIdentifier;
            return this;
        }

        /**
         * Builds immutable route auth details.
         *
         * @return route auth details
         */
        public ThingifierApiRouteAuthDetails build() {
            return new ThingifierApiRouteAuthDetails(this);
        }
    }
}
