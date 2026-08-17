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
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
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

public final class RestApiQueryHandler {

    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    public RestApiQueryHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    public RestApiQueryHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context) {
        return handle(
                url, queryParams, ApiRequestEnvelope.QueryBodyFormat.URL_ENCODED, "", context);
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingifierRequestContext context) {
        return handle(url, queryParams, queryBodyFormat, queryBody, context, null);
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ApiRequestEnvelope.QueryBodyFormat queryBodyFormat,
            final String queryBody,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingReadResultApiMapper apiMapper = new ThingReadResultApiMapper(runtime.apiConfig());
        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(url);

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

    private boolean shouldRemapQuery(final ThingifierApiLifecycleContext lifecycle) {
        return (lifecycle.queryParamsWereReplaced()
                        || lifecycle.rawBodyWasReplaced()
                        || lifecycle.queryBodyFormatWasReplaced())
                && !lifecycle.readQueryWasReplaced();
    }

    private ApiResponse methodNotAllowed(final String allowHeader) {
        return ApiResponse.error(405, "Method Not Allowed").setHeader("Allow", allowHeader);
    }

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

    private QueryFilterParams combine(
            final QueryFilterParams first, final QueryFilterParams second) {
        QueryFilterParams combined = new QueryFilterParams();
        combined.addAll(first);
        combined.addAll(second);
        return combined;
    }

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

    private ApiResponse advertiseQuery(final ApiResponse response) {
        return response.setHeader(
                ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
    }

    private static final class QueryMapping {

        private final ThingReadRequestMapping mapping;
        private final ApiResponse response;

        private QueryMapping(final ThingReadRequestMapping mapping, final ApiResponse response) {
            this.mapping = mapping;
            this.response = response;
        }

        private static QueryMapping mapping(final ThingReadRequestMapping mapping) {
            return new QueryMapping(mapping, null);
        }

        private static QueryMapping response(final ApiResponse response) {
            return new QueryMapping(null, response);
        }
    }
}
