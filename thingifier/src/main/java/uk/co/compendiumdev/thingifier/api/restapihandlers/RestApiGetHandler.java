package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

public class RestApiGetHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiGetHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    public RestApiGetHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final HttpHeadersBlock requestHeaders) {
        return handle(url, queryParams, runtime.contextFrom(requestHeaders));
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context) {
        return handle(url, queryParams, context, null);
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingReadResultApiMapper apiMapper = new ThingReadResultApiMapper(runtime.apiConfig());

        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(url);
        EffectiveQueryParams effectiveQueryParams =
                EffectiveQueryParams.forGet(runtime.apiConfig(), route, queryParams, url);
        if (effectiveQueryParams.isError()) {
            return apiMapper.map(effectiveQueryParams.error());
        }

        ThingReadRequestMapping mapping =
                new ThingReadRequestMapper(runtime.schema())
                        .map(route, effectiveQueryParams.queryParams());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        if (lifecycle == null) {
            RepositoryQueryResult queryResults =
                    runtime.queryService().execute(mapping.getQuery(), context.store());
            return advertiseQueryableCollections(route, apiMapper.map(url, queryResults));
        }

        lifecycle.useMappedReadQuery(mapping.getQuery());
        lifecycleHooks.runBeforeValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        if (lifecycle.queryParamsWereReplaced() && !lifecycle.readQueryWasReplaced()) {
            ThingReadRequestMapping remappedMapping =
                    mapReadQuery(url, route, lifecycle.queryParams());
            if (remappedMapping.isError()) {
                return apiMapper.map(remappedMapping.getError());
            }
            lifecycle.useMappedReadQuery(remappedMapping.getQuery());
        }

        lifecycleHooks.runAfterValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        lifecycleHooks.runBeforeActionHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return lifecycle.apiResponse();
        }

        ThingReadQuery activeQuery = lifecycle.readQuery();
        RepositoryQueryResult queryResults =
                runtime.queryService().execute(activeQuery, context.store());
        lifecycle.replaceReadQueryResult(queryResults);

        ApiResponse response =
                advertiseQueryableCollections(route, apiMapper.map(url, queryResults));
        lifecycle.useApiResponse(response);
        lifecycleHooks.runAfterActionHooks(lifecycle);

        RepositoryQueryResult finalResult =
                lifecycle.readQueryResult() == null ? queryResults : lifecycle.readQueryResult();
        if (finalResult != queryResults && !lifecycle.apiResponseWasReplaced()) {
            lifecycle.useApiResponse(
                    advertiseQueryableCollections(route, apiMapper.map(url, finalResult)));
        }
        return lifecycle.apiResponse();
    }

    private ThingReadRequestMapping mapReadQuery(
            final String url, final ThingRoute route, final QueryFilterParams queryParams) {
        EffectiveQueryParams effectiveQueryParams =
                EffectiveQueryParams.forGet(runtime.apiConfig(), route, queryParams, url);
        if (effectiveQueryParams.isError()) {
            return ThingReadRequestMapping.error(effectiveQueryParams.error());
        }
        return new ThingReadRequestMapper(runtime.schema())
                .map(route, effectiveQueryParams.queryParams());
    }

    private ApiResponse advertiseQueryableCollections(
            final ThingRoute route, final ApiResponse response) {
        if (route instanceof CollectionRoute || route instanceof RelationshipCollectionRoute) {
            response.setHeader(
                    ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                    ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
        }
        return response;
    }
}
