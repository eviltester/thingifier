package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Mutable operation context shared by all lifecycle hook phases for one Thingifier API request.
 *
 * <p>The class precomputes stable route details and tracks explicit replacements made by hooks. The
 * replacement flags allow handlers to decide whether request data should be remapped or whether a
 * hook intentionally supplied a final command, query, result, or response.
 */
public final class ThingifierApiLifecycleContext
        implements RouteMatchedContext,
                BodyParsedContext,
                BeforeValidationContext,
                AfterValidationContext,
                BeforeActionContext,
                AfterActionContext {

    private final ThingifierApiRuntime runtime;
    private final HttpApiRequest request;
    private final ThingifierHttpApi.HttpVerb effectiveVerb;
    private final RoutingVerb routingVerb;
    private final ThingRoute route;
    private final String apiPathPrefix;
    private final EntityDefinition targetEntity;
    private final EntityDefinition parentEntity;
    private final String targetIdentifier;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childIdentifier;

    private QueryFilterParams queryParams;
    private boolean queryParamsReplaced;
    private String rawBody;
    private boolean rawBodyReplaced;
    private ApiBodyFields bodyFields;
    private boolean bodyFieldsReplaced;
    private ApiRequestEnvelope.QueryBodyFormat queryBodyFormat;
    private boolean queryBodyFormatReplaced;
    private ThingifierRequestContext requestContext;
    private ThingWriteCommand writeCommand;
    private boolean writeCommandReplaced;
    private ThingReadQuery readQuery;
    private boolean readQueryReplaced;
    private ThingCommandResult validationResult;
    private ThingCommandResult writeCommandResult;
    private RepositoryQueryResult readQueryResult;
    private ApiResponse apiResponse;
    private boolean apiResponseReplaced;
    private boolean shortCircuit;

    /**
     * Creates a lifecycle context for one matched Thingifier API route.
     *
     * @param runtime runtime services for schema, API spec, stores, commands, and queries
     * @param request HTTP API request being processed
     * @param effectiveVerb verb after method override handling
     * @param routingVerb generated routing verb equivalent
     * @param route matched generated route
     * @param apiPathPrefix configured API prefix used by scoped hook matching
     */
    public ThingifierApiLifecycleContext(
            final ThingifierApiRuntime runtime,
            final HttpApiRequest request,
            final ThingifierHttpApi.HttpVerb effectiveVerb,
            final RoutingVerb routingVerb,
            final ThingRoute route,
            final String apiPathPrefix) {
        this.runtime = runtime;
        this.request = request;
        this.effectiveVerb = effectiveVerb;
        this.routingVerb = routingVerb;
        this.route = route;
        this.apiPathPrefix = apiPathPrefix == null ? "" : apiPathPrefix;
        this.queryParams = copyQueryParams(request.getFilterableQueryParams());
        this.rawBody = request.getBody();
        this.bodyFields = ApiBodyFields.empty();
        this.queryBodyFormat = ApiRequestEnvelope.QueryBodyFormat.URL_ENCODED;
        this.targetEntity = resolveTargetEntity(route);
        this.parentEntity = resolveParentEntity(route);
        this.targetIdentifier = resolveTargetIdentifier(route);
        this.parentIdentifier = resolveParentIdentifier(route);
        this.relationshipName = resolveRelationshipName(route);
        this.childIdentifier = resolveChildIdentifier(route);
    }

    @Override
    public ThingifierHttpApi.HttpVerb effectiveVerb() {
        return effectiveVerb;
    }

    /**
     * Returns the generated routing verb equivalent for scoped hook matching.
     *
     * @return routing verb, or null when no generated route verb applies
     */
    public RoutingVerb routingVerb() {
        return routingVerb;
    }

    /**
     * Returns runtime services backing this lifecycle request.
     *
     * @return Thingifier API runtime
     */
    public ThingifierApiRuntime runtime() {
        return runtime;
    }

    /**
     * Returns the underlying HTTP API request.
     *
     * @return request being processed
     */
    public HttpApiRequest request() {
        return request;
    }

    @Override
    public String path() {
        return request.getPath();
    }

    /**
     * Returns the API path prefix used when matching scoped hooks and API spec rules.
     *
     * @return configured API prefix, or an empty string
     */
    public String apiPathPrefix() {
        return apiPathPrefix;
    }

    @Override
    public ThingRoute route() {
        return route;
    }

    @Override
    public EntityDefinition targetEntity() {
        return targetEntity;
    }

    @Override
    public String targetIdentifier() {
        return targetIdentifier;
    }

    @Override
    public EntityDefinition parentEntity() {
        return parentEntity;
    }

    @Override
    public String parentIdentifier() {
        return parentIdentifier;
    }

    @Override
    public String relationshipName() {
        return relationshipName;
    }

    @Override
    public String childIdentifier() {
        return childIdentifier;
    }

    @Override
    public HttpHeadersBlock headers() {
        return request.getHeaders();
    }

    @Override
    public void setHeader(final String name, final String value) {
        request.addHeader(name, value);
        requestContext = null;
    }

    @Override
    public QueryFilterParams queryParams() {
        return copyQueryParams(queryParams);
    }

    @Override
    public void replaceQueryParams(final QueryFilterParams queryParams) {
        this.queryParams = copyQueryParams(queryParams);
        this.queryParamsReplaced = true;
    }

    @Override
    public String rawBody() {
        return rawBody;
    }

    @Override
    public void replaceRawBody(final String rawBody) {
        this.rawBody = rawBody == null ? "" : rawBody;
        this.rawBodyReplaced = true;
        request.setBody(this.rawBody);
    }

    @Override
    public ApiBodyFields bodyFields() {
        return bodyFields;
    }

    @Override
    public void replaceBodyFields(final ApiBodyFields bodyFields) {
        this.bodyFields = bodyFields == null ? ApiBodyFields.empty() : bodyFields;
        this.bodyFieldsReplaced = true;
    }

    @Override
    public ApiRequestEnvelope.QueryBodyFormat queryBodyFormat() {
        return queryBodyFormat;
    }

    @Override
    public void replaceQueryBodyFormat(final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat) {
        if (queryBodyFormat != null) {
            this.queryBodyFormat = queryBodyFormat;
            this.queryBodyFormatReplaced = true;
        }
    }

    @Override
    public ThingifierRequestContext requestContext() {
        if (requestContext == null) {
            requestContext = runtime.contextFrom(request.getHeaders());
        }
        return requestContext;
    }

    @Override
    public String dataScopeName() {
        return requestContext().dataScopeName();
    }

    @Override
    public ThingStore store() {
        return requestContext().store();
    }

    @Override
    public ThingWriteCommand writeCommand() {
        return writeCommand;
    }

    @Override
    public void replaceWriteCommand(final ThingWriteCommand command) {
        this.writeCommand = command;
        this.writeCommandReplaced = true;
    }

    @Override
    public ThingReadQuery readQuery() {
        return readQuery;
    }

    @Override
    public void replaceReadQuery(final ThingReadQuery query) {
        this.readQuery = query;
        this.readQueryReplaced = true;
    }

    @Override
    public ThingCommandResult validationResult() {
        return validationResult;
    }

    @Override
    public void replaceValidationResult(final ThingCommandResult result) {
        this.validationResult = result;
    }

    @Override
    public void clearValidationResult() {
        validationResult = null;
    }

    @Override
    public ThingCommandResult writeCommandResult() {
        return writeCommandResult;
    }

    @Override
    public void replaceWriteCommandResult(final ThingCommandResult result) {
        this.writeCommandResult = result;
    }

    @Override
    public RepositoryQueryResult readQueryResult() {
        return readQueryResult;
    }

    @Override
    public void replaceReadQueryResult(final RepositoryQueryResult result) {
        this.readQueryResult = result;
    }

    @Override
    public ApiResponse apiResponse() {
        return apiResponse;
    }

    @Override
    public void replaceApiResponse(final ApiResponse response) {
        this.apiResponse = response;
        this.apiResponseReplaced = true;
    }

    @Override
    public void shortCircuitWith(final ApiResponse response) {
        this.apiResponse = response;
        this.apiResponseReplaced = true;
        this.shortCircuit = true;
    }

    @Override
    public boolean shouldShortCircuit() {
        return shortCircuit;
    }

    /**
     * Applies parsed request data after syntax and entity-view checks.
     *
     * <p>Values already replaced by earlier hooks are preserved so explicit hook mutations are not
     * overwritten by normal parsing.
     *
     * @param envelope parsed request envelope
     */
    public void applyParsedEnvelope(final ApiRequestEnvelope envelope) {
        if (envelope == null) {
            return;
        }
        if (!queryParamsReplaced) {
            queryParams = copyQueryParams(envelope.queryParams());
        }
        if (!bodyFieldsReplaced) {
            bodyFields = envelope.bodyFields();
        }
        rawBody = envelope.body();
        queryBodyFormat = envelope.queryBodyFormat();
    }

    /**
     * Converts the current lifecycle request state back into a handler envelope.
     *
     * @return request envelope reflecting hook mutations
     */
    public ApiRequestEnvelope toEnvelope() {
        return ApiRequestEnvelope.fromParsed(
                effectiveVerb,
                path(),
                queryParams(),
                headers(),
                bodyFields,
                rawBody,
                queryBodyFormat);
    }

    /**
     * Stores the write command produced by normal request mapping.
     *
     * <p>This differs from {@link #replaceWriteCommand(ThingWriteCommand)} because it records the
     * mapped baseline, not a hook replacement.
     *
     * @param command mapped write command
     */
    public void useMappedWriteCommand(final ThingWriteCommand command) {
        this.writeCommand = command;
        this.writeCommandReplaced = false;
    }

    /**
     * Reports whether a hook explicitly replaced the write command.
     *
     * @return true when the mapped write command was replaced
     */
    public boolean writeCommandWasReplaced() {
        return writeCommandReplaced;
    }

    /**
     * Stores the read query produced by normal request mapping.
     *
     * <p>This differs from {@link #replaceReadQuery(ThingReadQuery)} because it records the mapped
     * baseline, not a hook replacement.
     *
     * @param query mapped read query
     */
    public void useMappedReadQuery(final ThingReadQuery query) {
        this.readQuery = query;
        this.readQueryReplaced = false;
    }

    /**
     * Reports whether a hook explicitly replaced the read query.
     *
     * @return true when the mapped read query was replaced
     */
    public boolean readQueryWasReplaced() {
        return readQueryReplaced;
    }

    /**
     * Reports whether query parameters were explicitly replaced.
     *
     * @return true when a hook replaced query parameters
     */
    public boolean queryParamsWereReplaced() {
        return queryParamsReplaced;
    }

    /**
     * Reports whether parsed body fields were explicitly replaced.
     *
     * @return true when a hook replaced body fields
     */
    public boolean bodyFieldsWereReplaced() {
        return bodyFieldsReplaced;
    }

    /**
     * Reports whether the raw body was explicitly replaced.
     *
     * @return true when a hook replaced raw body text
     */
    public boolean rawBodyWasReplaced() {
        return rawBodyReplaced;
    }

    /**
     * Reports whether the QUERY body format was explicitly replaced.
     *
     * @return true when a hook replaced query body format
     */
    public boolean queryBodyFormatWasReplaced() {
        return queryBodyFormatReplaced;
    }

    /**
     * Stores the API response produced by normal request processing.
     *
     * <p>This differs from {@link #replaceApiResponse(ApiResponse)} because it records the mapped
     * baseline, not a hook replacement.
     *
     * @param response generated API response
     */
    public void useApiResponse(final ApiResponse response) {
        this.apiResponse = response;
        this.apiResponseReplaced = false;
    }

    /**
     * Reports whether a hook explicitly replaced the API response.
     *
     * @return true when a hook replaced the API response
     */
    public boolean apiResponseWasReplaced() {
        return apiResponseReplaced;
    }

    /**
     * Resolves the target entity for entity and relationship routes.
     *
     * @param route matched generated route
     * @return target entity, or null when the route is not entity-backed
     */
    private EntityDefinition resolveTargetEntity(final ThingRoute route) {
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

    /**
     * Resolves the parent entity for relationship routes.
     *
     * @param route matched generated route
     * @return parent entity, or null for non-relationship routes
     */
    private EntityDefinition resolveParentEntity(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return entityNamed(((RelationshipCollectionRoute) route).parentEntity().name());
        }
        if (route instanceof RelationshipInstanceRoute) {
            return entityNamed(((RelationshipInstanceRoute) route).parentEntity().name());
        }
        return null;
    }

    /**
     * Resolves the target identifier for instance routes.
     *
     * @param route matched generated route
     * @return target identifier, or null for collection routes
     */
    private String resolveTargetIdentifier(final ThingRoute route) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    /**
     * Resolves the parent identifier for relationship routes.
     *
     * @param route matched generated route
     * @return parent identifier, or null for non-relationship routes
     */
    private String resolveParentIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentIdentifier();
        }
        return null;
    }

    /**
     * Resolves the relationship name for relationship routes.
     *
     * @param route matched generated route
     * @return relationship name, or null for entity routes
     */
    private String resolveRelationshipName(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).relationshipName();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).relationshipName();
        }
        return null;
    }

    /**
     * Resolves the child identifier for relationship instance routes.
     *
     * @param route matched generated route
     * @return child identifier, or null when the route does not identify a child
     */
    private String resolveChildIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    /**
     * Resolves the related entity targeted by a relationship route.
     *
     * @param parentEntity parent entity reference from the route
     * @param relationshipName relationship route name
     * @return related target entity, or null when the relationship cannot be resolved
     */
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

    /**
     * Looks up an entity definition by singular or plural model name.
     *
     * @param entityName singular or plural entity name
     * @return matching entity definition, or null
     */
    private EntityDefinition entityNamed(final String entityName) {
        return runtime.schema().definitionWithSingularOrPluralNamed(entityName);
    }

    /**
     * Copies query parameters so hook callers cannot mutate internal state by retaining references.
     *
     * @param original source query parameters
     * @return independent query parameter copy
     */
    private QueryFilterParams copyQueryParams(final QueryFilterParams original) {
        QueryFilterParams copy = new QueryFilterParams();
        copy.addAll(original);
        return copy;
    }
}
