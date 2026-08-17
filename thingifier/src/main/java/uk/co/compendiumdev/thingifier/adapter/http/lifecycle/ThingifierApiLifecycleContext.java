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

    public RoutingVerb routingVerb() {
        return routingVerb;
    }

    public ThingifierApiRuntime runtime() {
        return runtime;
    }

    public HttpApiRequest request() {
        return request;
    }

    @Override
    public String path() {
        return request.getPath();
    }

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

    public void useMappedWriteCommand(final ThingWriteCommand command) {
        this.writeCommand = command;
        this.writeCommandReplaced = false;
    }

    public boolean writeCommandWasReplaced() {
        return writeCommandReplaced;
    }

    public void useMappedReadQuery(final ThingReadQuery query) {
        this.readQuery = query;
        this.readQueryReplaced = false;
    }

    public boolean readQueryWasReplaced() {
        return readQueryReplaced;
    }

    public boolean queryParamsWereReplaced() {
        return queryParamsReplaced;
    }

    public boolean bodyFieldsWereReplaced() {
        return bodyFieldsReplaced;
    }

    public boolean rawBodyWasReplaced() {
        return rawBodyReplaced;
    }

    public boolean queryBodyFormatWasReplaced() {
        return queryBodyFormatReplaced;
    }

    public void useApiResponse(final ApiResponse response) {
        this.apiResponse = response;
        this.apiResponseReplaced = false;
    }

    public boolean apiResponseWasReplaced() {
        return apiResponseReplaced;
    }

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

    private EntityDefinition resolveParentEntity(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return entityNamed(((RelationshipCollectionRoute) route).parentEntity().name());
        }
        if (route instanceof RelationshipInstanceRoute) {
            return entityNamed(((RelationshipInstanceRoute) route).parentEntity().name());
        }
        return null;
    }

    private String resolveTargetIdentifier(final ThingRoute route) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    private String resolveParentIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentIdentifier();
        }
        return null;
    }

    private String resolveRelationshipName(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).relationshipName();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).relationshipName();
        }
        return null;
    }

    private String resolveChildIdentifier(final ThingRoute route) {
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

    private QueryFilterParams copyQueryParams(final QueryFilterParams original) {
        QueryFilterParams copy = new QueryFilterParams();
        copy.addAll(original);
        return copy;
    }
}
