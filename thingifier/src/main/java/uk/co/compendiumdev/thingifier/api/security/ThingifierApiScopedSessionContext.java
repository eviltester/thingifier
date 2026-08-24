package uk.co.compendiumdev.thingifier.api.security;

import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Immutable request context passed to a scoped-session resolver.
 *
 * <p>The context exposes request facts and route target details so application code can validate a
 * session credential before returning a trusted principal and data-scope selection. It deliberately
 * does not treat the credential value itself as a store name.
 */
public final class ThingifierApiScopedSessionContext {

    private final String sessionName;
    private final String credential;
    private final ThingifierApiScopedSessionCredentialSourceType credentialSourceType;
    private final String credentialSourceName;
    private final RoutingVerb verb;
    private final String path;
    private final Map<String, String> pathParameters;
    private final ThingRoute route;
    private final HttpHeadersBlock headers;
    private final QueryFilterParams queryParams;
    private final ThingifierRequestContext requestContext;
    private final EntityDefinition targetEntity;
    private final String targetIdentifier;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    private ThingifierApiScopedSessionContext(final Builder builder) {
        this.sessionName = builder.sessionName;
        this.credential = builder.credential;
        this.credentialSourceType = builder.credentialSourceType;
        this.credentialSourceName = builder.credentialSourceName;
        this.verb = builder.verb;
        this.path = builder.path;
        this.pathParameters = Map.copyOf(builder.pathParameters);
        this.route = builder.route;
        this.headers = copyHeaders(builder.headers);
        this.queryParams = copyQueryParams(builder.queryParams);
        this.requestContext = builder.requestContext;
        this.targetEntity = builder.targetEntity;
        this.targetIdentifier = builder.targetIdentifier;
        this.parentEntity = builder.parentEntity;
        this.parentIdentifier = builder.parentIdentifier;
        this.relationshipName = builder.relationshipName;
        this.childIdentifier = builder.childIdentifier;
    }

    /**
     * Creates a builder for a resolver context.
     *
     * @return mutable builder used by Thingifier runtime policy
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return scoped-session definition name
     */
    public String sessionName() {
        return sessionName;
    }

    /**
     * @return credential value read from the configured request source
     */
    public String credential() {
        return credential;
    }

    /**
     * @return kind of request source that supplied the credential
     */
    public ThingifierApiScopedSessionCredentialSourceType credentialSourceType() {
        return credentialSourceType;
    }

    /**
     * @return configured header, query parameter, or cookie name
     */
    public String credentialSourceName() {
        return credentialSourceName;
    }

    /**
     * @return generated routing verb for this request
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * @return public request path after API prefix handling
     */
    public String path() {
        return path;
    }

    /**
     * @return matched path parameters, if any
     */
    public Map<String, String> pathParameters() {
        return pathParameters;
    }

    /**
     * @return resolved Thingifier route
     */
    public ThingRoute route() {
        return route;
    }

    /**
     * @return copy of request headers
     */
    public HttpHeadersBlock headers() {
        return copyHeaders(headers);
    }

    /**
     * @return copy of query parameters available at session resolution time
     */
    public QueryFilterParams queryParams() {
        return copyQueryParams(queryParams);
    }

    /**
     * Returns the mutable request context for advanced application checks.
     *
     * <p>Resolver code should treat this as read-only. Thingifier applies the final selected scope
     * after the resolver returns so later validators, authorizers, and handlers are aligned.
     *
     * @return active request context before scoped-session selection is applied
     */
    public ThingifierRequestContext requestContext() {
        return requestContext;
    }

    /**
     * @return active data-scope name before this resolver result is applied
     */
    public String dataScopeName() {
        return requestContext == null ? null : requestContext.dataScopeName();
    }

    /**
     * @return active store before this resolver result is applied
     */
    public ThingStore store() {
        return requestContext == null ? null : requestContext.store();
    }

    /**
     * @return target entity for entity and relationship routes
     */
    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    /**
     * @return target identifier for instance routes, or null for collection routes
     */
    public String targetIdentifier() {
        return targetIdentifier;
    }

    /**
     * @return parent entity for relationship routes, or null for entity routes
     */
    public EntityDefinition parentEntity() {
        return parentEntity;
    }

    /**
     * @return parent identifier for relationship routes, or null for entity routes
     */
    public String parentIdentifier() {
        return parentIdentifier;
    }

    /**
     * @return relationship name for relationship routes, or null for entity routes
     */
    public String relationshipName() {
        return relationshipName;
    }

    /**
     * @return child identifier for relationship instance routes, or null when absent
     */
    public String childIdentifier() {
        return childIdentifier;
    }

    private static HttpHeadersBlock copyHeaders(final HttpHeadersBlock original) {
        HttpHeadersBlock copy = new HttpHeadersBlock();
        if (original != null) {
            copy.putAll(original);
        }
        return copy;
    }

    private static QueryFilterParams copyQueryParams(final QueryFilterParams original) {
        QueryFilterParams copy = new QueryFilterParams();
        copy.addAll(original);
        return copy;
    }

    /** Builder used by runtime policy to assemble immutable resolver context. */
    public static final class Builder {
        private String sessionName;
        private String credential;
        private ThingifierApiScopedSessionCredentialSourceType credentialSourceType;
        private String credentialSourceName;
        private RoutingVerb verb;
        private String path;
        private Map<String, String> pathParameters = new HashMap<>();
        private ThingRoute route;
        private HttpHeadersBlock headers;
        private QueryFilterParams queryParams;
        private ThingifierRequestContext requestContext;
        private EntityDefinition targetEntity;
        private String targetIdentifier;
        private EntityDefinition parentEntity;
        private String parentIdentifier;
        private String relationshipName;
        private String childIdentifier;

        /**
         * @param sessionName scoped-session definition name
         */
        public Builder sessionName(final String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        /**
         * @param credential credential value from the configured source
         */
        public Builder credential(final String credential) {
            this.credential = credential;
            return this;
        }

        /**
         * @param credentialSourceType kind of request source that supplied the credential
         */
        public Builder credentialSourceType(
                final ThingifierApiScopedSessionCredentialSourceType credentialSourceType) {
            this.credentialSourceType = credentialSourceType;
            return this;
        }

        /**
         * @param credentialSourceName configured header, query parameter, or cookie name
         */
        public Builder credentialSourceName(final String credentialSourceName) {
            this.credentialSourceName = credentialSourceName;
            return this;
        }

        /**
         * @param verb generated routing verb
         */
        public Builder verb(final RoutingVerb verb) {
            this.verb = verb;
            return this;
        }

        /**
         * @param path public request path
         */
        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        /**
         * @param pathParameters matched path parameters
         */
        public Builder pathParameters(final Map<String, String> pathParameters) {
            this.pathParameters = pathParameters == null ? new HashMap<>() : pathParameters;
            return this;
        }

        /**
         * @param route resolved Thingifier route
         */
        public Builder route(final ThingRoute route) {
            this.route = route;
            return this;
        }

        /**
         * @param headers request headers
         */
        public Builder headers(final HttpHeadersBlock headers) {
            this.headers = headers;
            return this;
        }

        /**
         * @param queryParams request query parameters
         */
        public Builder queryParams(final QueryFilterParams queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        /**
         * @param requestContext active request context
         */
        public Builder requestContext(final ThingifierRequestContext requestContext) {
            this.requestContext = requestContext;
            return this;
        }

        /**
         * @param targetEntity target entity
         */
        public Builder targetEntity(final EntityDefinition targetEntity) {
            this.targetEntity = targetEntity;
            return this;
        }

        /**
         * @param targetIdentifier target identifier
         */
        public Builder targetIdentifier(final String targetIdentifier) {
            this.targetIdentifier = targetIdentifier;
            return this;
        }

        /**
         * @param parentEntity relationship parent entity
         */
        public Builder parentEntity(final EntityDefinition parentEntity) {
            this.parentEntity = parentEntity;
            return this;
        }

        /**
         * @param parentIdentifier relationship parent identifier
         */
        public Builder parentIdentifier(final String parentIdentifier) {
            this.parentIdentifier = parentIdentifier;
            return this;
        }

        /**
         * @param relationshipName relationship name
         */
        public Builder relationshipName(final String relationshipName) {
            this.relationshipName = relationshipName;
            return this;
        }

        /**
         * @param childIdentifier relationship child identifier
         */
        public Builder childIdentifier(final String childIdentifier) {
            this.childIdentifier = childIdentifier;
            return this;
        }

        /**
         * @return immutable scoped-session resolver context
         */
        public ThingifierApiScopedSessionContext build() {
            return new ThingifierApiScopedSessionContext(this);
        }
    }
}
