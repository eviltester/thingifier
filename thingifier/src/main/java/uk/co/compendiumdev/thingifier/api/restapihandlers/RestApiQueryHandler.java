package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiMappingError;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

/**
 * Handles Thingifier QUERY operations for collection and relationship collection routes.
 *
 * <p>QUERY supports URL-encoded filters, structured JSON queries, and JSONPath filtering. The
 * handler also participates in lifecycle hooks so callers can inspect or replace the mapped read
 * query before it reaches the query service.
 */
public final class RestApiQueryHandler {

    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a query handler with no lifecycle hooks.
     *
     * @param runtime runtime services used to map and execute queries
     */
    public RestApiQueryHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a query handler with lifecycle hooks.
     *
     * @param runtime runtime services used to map and execute queries
     * @param lifecycleHooks lifecycle hooks for validation and action phases
     */
    public RestApiQueryHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a URL-encoded query using direct-call arguments.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param context request context containing the active store
     * @return API response for the query
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context) {
        return handle(
                url, queryParams, ApiRequestEnvelope.QueryBodyFormat.URL_ENCODED, "", context);
    }

    /**
     * Handles a query without lifecycle hook state.
     *
     * @param url generated API path
     * @param queryParams URI query filters
     * @param queryBodyFormat body format selected from content type
     * @param queryBody raw query body
     * @param context request context containing the active store
     * @return API response for the query
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingifierRequestContext context) {
        return handle(url, queryParams, queryBodyFormat, queryBody, context, null);
    }

    /**
     * Handles a query with optional lifecycle hook state.
     *
     * <p>When lifecycle state is present, hooks can replace query params, raw body, query format,
     * or the mapped read query before execution, then inspect or replace the query result
     * afterwards.
     *
     * @param url generated API path
     * @param queryParams URI query filters
     * @param queryBodyFormat body format selected from content type
     * @param queryBody raw query body
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the query
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingReadResultApiMapper apiMapper = new ThingReadResultApiMapper(runtime.apiConfig());
        ThingRoute route = routeFor(url, lifecycle);

        if (route instanceof InstanceRoute) {
            return methodNotAllowed("OPTIONS, GET, HEAD, POST, PUT, DELETE");
        }

        if (route instanceof RelationshipInstanceRoute) {
            return methodNotAllowed("OPTIONS, DELETE");
        }

        if (!(route instanceof CollectionRoute || route instanceof RelationshipCollectionRoute)) {
            return apiMapper.map(
                    ApiMappingError.withMessage(
                            404, String.format("Could not find an instance with %s", url)));
        }

        QueryMapping mapping = mapQuery(route, queryParams, queryBodyFormat, queryBody, apiMapper);
        if (mapping.response != null) {
            return mapping.response;
        }

        if (lifecycle == null) {
            return executeMappedQuery(url, mapping, queryBodyFormat, queryBody, context, apiMapper);
        }

        lifecycle.useMappedReadQuery(mapping.mapping.getQuery());
        lifecycleHooks.runBeforeValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        if (shouldRemapQuery(lifecycle)) {
            QueryMapping remappedMapping =
                    mapQuery(
                            route,
                            lifecycle.queryParams(),
                            lifecycle.queryBodyFormat(),
                            lifecycle.rawBody(),
                            apiMapper);
            if (remappedMapping.response != null) {
                return remappedMapping.response;
            }
            lifecycle.useMappedReadQuery(remappedMapping.mapping.getQuery());
        }

        lifecycleHooks.runAfterValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        lifecycleHooks.runBeforeActionHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        RepositoryQueryResult queryResults =
                runtime.queryService().execute(lifecycle.readQuery(), context.store(), false);
        lifecycle.replaceReadQueryResult(queryResults);

        ApiResponse response =
                responseForQuery(
                        url,
                        lifecycle.queryBodyFormat(),
                        lifecycle.rawBody(),
                        queryResults,
                        context,
                        apiMapper);
        lifecycle.useApiResponse(response);
        lifecycleHooks.runAfterActionHooks(lifecycle);

        RepositoryQueryResult finalResult =
                lifecycle.readQueryResult() == null ? queryResults : lifecycle.readQueryResult();
        if (finalResult != queryResults && !lifecycle.apiResponseWasReplaced()) {
            lifecycle.useApiResponse(
                    responseForQuery(
                            url,
                            lifecycle.queryBodyFormat(),
                            lifecycle.rawBody(),
                            finalResult,
                            context,
                            apiMapper));
        }
        return lifecycle.apiResponse();
    }

    private ThingRoute routeFor(final String url, final ThingifierApiLifecycleContext lifecycle) {
        return lifecycle == null ? runtime.routeFor(RoutingVerb.QUERY, url) : lifecycle.route();
    }

    /**
     * Maps request query data into a Thingifier read request.
     *
     * <p>Structured JSON query bodies are compiled into query params before normal effective-query
     * processing so the rest of the mapper pipeline can remain shared.
     *
     * @param route mapped generated route
     * @param queryParams URI or merged query filters
     * @param queryBodyFormat body format selected from content type
     * @param queryBody raw query body
     * @param apiMapper mapper used to convert mapping errors to API responses
     * @return query mapping or early API response
     */
    private QueryMapping mapQuery(
            final ThingRoute route,
            final QueryFilterParams queryParams,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingReadResultApiMapper apiMapper) {
        QueryFilterParams requestedQueryParams = queryParams;
        if (queryBodyFormat == ApiRequestEnvelope.QueryBodyFormat.STRUCTURED_JSON) {
            StructuredJsonQueryCompiler.Result structuredQuery =
                    StructuredJsonQueryCompiler.compile(queryBody, resultEntityFor(route));
            if (structuredQuery.isError()) {
                return QueryMapping.response(
                        advertiseQuery(apiMapper.map(structuredQuery.error())));
            }
            requestedQueryParams = combine(queryParams, structuredQuery.queryParams());
        }

        EffectiveQueryParams effectiveQueryParams =
                EffectiveQueryParams.forQuery(runtime.apiConfig(), route, requestedQueryParams);
        if (effectiveQueryParams.isError()) {
            return QueryMapping.response(apiMapper.map(effectiveQueryParams.error()));
        }

        ThingReadRequestMapping mapping =
                new ThingReadRequestMapper(runtime.schema())
                        .map(route, effectiveQueryParams.queryParams());
        if (mapping.isError()) {
            return QueryMapping.response(apiMapper.map(mapping.getError()));
        }
        return QueryMapping.mapping(mapping);
    }

    /**
     * Executes a mapped query when no lifecycle hook can replace it.
     *
     * @param url generated API path
     * @param mapping mapped read request
     * @param queryBodyFormat body format selected from content type
     * @param queryBody raw query body
     * @param context request context containing the active store
     * @param apiMapper mapper used to convert query results to API responses
     * @return API response for the executed query
     */
    private ApiResponse executeMappedQuery(
            final String url,
            final QueryMapping mapping,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingifierRequestContext context,
            final ThingReadResultApiMapper apiMapper) {
        RepositoryQueryResult queryResults =
                runtime.queryService().execute(mapping.mapping.getQuery(), context.store(), false);
        return responseForQuery(url, queryBodyFormat, queryBody, queryResults, context, apiMapper);
    }

    /**
     * Converts query results to an API response and applies query response features.
     *
     * <p>JSONPath filtering happens after entity response view selection so JSONPath sees the
     * public response shape rather than fields hidden by configured views.
     *
     * @param url generated API path
     * @param queryBodyFormat body format selected from content type
     * @param queryBody raw query body
     * @param queryResults repository query result
     * @param context request context containing the active store
     * @param apiMapper mapper used to convert query results to API responses
     * @return API response with QUERY advertising headers
     */
    private ApiResponse responseForQuery(
            final String url,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final RepositoryQueryResult queryResults,
            final ThingifierRequestContext context,
            final ThingReadResultApiMapper apiMapper) {
        ApiResponse response = apiMapper.map(url, queryResults);
        if (response.isErrorResponse()) {
            return advertiseQuery(response);
        }

        if (queryBodyFormat == ApiRequestEnvelope.QueryBodyFormat.JSONPATH) {
            final EntityViewDefinition responseView = responseViewFor(url, response);
            response =
                    new JsonPathQueryFilter(new JsonThing(runtime.apiConfig().jsonOutput()))
                            .filter(queryResults, context, queryBody, responseView);
        }

        return advertiseQuery(response);
    }

    /**
     * Reports whether hook mutations require query remapping.
     *
     * @param lifecycle lifecycle context after before-validation hooks
     * @return true when request data changed but the mapped read query was not explicitly replaced
     */
    private boolean shouldRemapQuery(final ThingifierApiLifecycleContext lifecycle) {
        return (lifecycle.queryParamsWereReplaced()
                        || lifecycle.rawBodyWasReplaced()
                        || lifecycle.queryBodyFormatWasReplaced())
                && !lifecycle.readQueryWasReplaced();
    }

    /**
     * Creates the standard 405 response for routes where QUERY is not meaningful.
     *
     * @param allowHeader value to expose in the Allow header
     * @return method-not-allowed response
     */
    private ApiResponse methodNotAllowed(final String allowHeader) {
        return ApiResponse.error(405, "Method Not Allowed").setHeader("Allow", allowHeader);
    }

    /**
     * Resolves the response view that should be applied before JSONPath filtering.
     *
     * @param url generated API path
     * @param response mapped query response
     * @return entity view to apply during filtering, or null
     */
    private EntityViewDefinition responseViewFor(final String url, final ApiResponse response) {
        if (response == null
                || response.isErrorResponse()
                || response.hasABodyOverride()
                || response.getTypeOfThingReturned() == null) {
            return null;
        }

        final EntityDefinition entity = response.getTypeOfThingReturned();
        return runtime.apiSpec()
                .responseEntityViewFor(
                        RoutingVerb.QUERY,
                        url,
                        runtime.apiConfig().getApiEndPointPrefix(),
                        entity,
                        response.getStatusCode())
                .filter(entity::hasViewNamed)
                .map(entity::getViewNamed)
                .orElse(null);
    }

    /**
     * Combines two query parameter sets while preserving both sources.
     *
     * @param first first query parameter set
     * @param second second query parameter set
     * @return combined query parameters
     */
    private QueryFilterParams combine(
            final QueryFilterParams first, final QueryFilterParams second) {
        QueryFilterParams combined = new QueryFilterParams();
        combined.addAll(first);
        combined.addAll(second);
        return combined;
    }

    /**
     * Resolves the entity type that a QUERY route returns.
     *
     * @param route mapped collection or relationship collection route
     * @return result entity definition, or null when the route cannot be resolved
     */
    private EntityDefinition resultEntityFor(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return runtime.schema()
                    .definitionWithSingularOrPluralNamed(((CollectionRoute) route).entity().name());
        }

        if (route instanceof RelationshipCollectionRoute) {
            RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            EntityDefinition parent =
                    runtime.schema()
                            .definitionWithSingularOrPluralNamed(
                                    relationship.parentEntity().name());
            if (parent == null) {
                return null;
            }
            for (RelationshipVectorDefinition vector :
                    parent.related().getRelationships(relationship.relationshipName())) {
                if (vector.getFrom() == parent) {
                    return vector.getTo();
                }
                if (vector.getTo() == parent) {
                    return vector.getFrom();
                }
            }
        }

        return null;
    }

    /**
     * Adds supported QUERY media type information to the response.
     *
     * @param response response to update
     * @return same response with Accept-Query header set
     */
    private ApiResponse advertiseQuery(final ApiResponse response) {
        return response.setHeader(
                ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
    }

    private static final class QueryMapping {

        private final ThingReadRequestMapping mapping;
        private final ApiResponse response;

        /**
         * Creates a query mapping value that holds either a mapped request or an early response.
         *
         * @param mapping mapped read request, or null when response is populated
         * @param response early API response, or null when mapping is populated
         */
        private QueryMapping(final ThingReadRequestMapping mapping, final ApiResponse response) {
            this.mapping = mapping;
            this.response = response;
        }

        /**
         * Creates a successful query mapping.
         *
         * @param mapping mapped read request
         * @return query mapping wrapper
         */
        private static QueryMapping mapping(final ThingReadRequestMapping mapping) {
            return new QueryMapping(mapping, null);
        }

        /**
         * Creates a query mapping result that should immediately return a response.
         *
         * @param response API response to return
         * @return query mapping wrapper containing the response
         */
        private static QueryMapping response(final ApiResponse response) {
            return new QueryMapping(null, response);
        }
    }
}
