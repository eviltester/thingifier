package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiOperationValidationPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

/**
 * Handles generated Thingifier GET routes.
 *
 * <p>The handler maps route and query parameters to a read query. When lifecycle hooks are active,
 * hooks can replace query parameters or the mapped read query before execution and inspect or
 * replace the repository result afterwards.
 */
public class RestApiGetHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a GET handler from a Thingifier model.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public RestApiGetHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a GET handler with no lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     */
    public RestApiGetHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a GET handler with lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     * @param lifecycleHooks lifecycle hooks for read processing
     */
    public RestApiGetHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a GET request using raw headers to resolve context.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param requestHeaders request headers used to resolve context
     * @return API response for the GET
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final HttpHeadersBlock requestHeaders) {
        return handle(url, queryParams, runtime.contextFrom(requestHeaders));
    }

    /**
     * Handles a GET request without lifecycle hook state.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param context request context containing the active store
     * @return API response for the GET
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context) {
        return handle(RoutingVerb.GET, url, queryParams, context, null);
    }

    /**
     * Handles a GET-compatible request using an explicit routing verb.
     *
     * <p>HEAD uses the same repository lookup as GET but may have a different route rule, so direct
     * callers can supply the verb that selected the public route.
     *
     * @param verb routing verb used to resolve route rules
     * @param url generated or fixed public API path
     * @param queryParams query filters to apply
     * @param context request context containing the active store
     * @return API response for the read
     */
    public ApiResponse handle(
            final RoutingVerb verb,
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context) {
        return handle(verb, url, queryParams, context, null);
    }

    /**
     * Handles a GET request with optional lifecycle hook state.
     *
     * @param url generated API path
     * @param queryParams query filters to apply
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the GET
     */
    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        final RoutingVerb verb = lifecycle == null ? RoutingVerb.GET : lifecycle.routingVerb();
        return handle(verb, url, queryParams, context, lifecycle);
    }

    private ApiResponse handle(
            final RoutingVerb verb,
            final String url,
            final QueryFilterParams queryParams,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingReadResultApiMapper apiMapper = new ThingReadResultApiMapper(runtime.apiConfig());

        ThingRoute route = lifecycle == null ? runtime.routeFor(verb, url) : lifecycle.route();
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

        ApiResponse operationValidationResponse =
                new ApiOperationValidationPolicy(runtime)
                        .rejectIfInvalid(
                                verb,
                                url,
                                route,
                                context,
                                ApiBodyFields.empty(),
                                "",
                                effectiveQueryParams.queryParams(),
                                readOperationType(verb),
                                lifecycle);
        if (operationValidationResponse != null) {
            return operationValidationResponse;
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

    /**
     * Remaps a read query after hooks replace query parameters.
     *
     * @param url generated API path
     * @param route mapped generated route
     * @param queryParams replacement query filters
     * @return mapped read request or mapping error
     */
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

    /**
     * Adds Accept-Query advertising to collection routes which can be queried.
     *
     * @param route mapped generated route
     * @param response response to update
     * @return same response with query advertising where applicable
     */
    private ApiResponse advertiseQueryableCollections(
            final ThingRoute route, final ApiResponse response) {
        if (route instanceof CollectionRoute || route instanceof RelationshipCollectionRoute) {
            response.setHeader(
                    ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                    ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
        }
        return response;
    }

    private String readOperationType(final RoutingVerb verb) {
        return verb == RoutingVerb.QUERY ? "QUERY" : "READ";
    }
}
