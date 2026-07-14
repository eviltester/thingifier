package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiMappingError;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

public class RestApiGetHandler {
    private final ThingifierApiRuntime runtime;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiGetHandler(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
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
        ThingReadResultApiMapper apiMapper = new ThingReadResultApiMapper(runtime.apiConfig());
        // if there are params, and we are not allowed to filter, and we enforce that
        if (queryParams.size() > 0
                && runtime.apiConfig().forParams().willEnforceFilteringThroughUrlParams()
                && !runtime.apiConfig().forParams().willAllowFilteringThroughUrlParams()) {
            return apiMapper.map(
                    ApiMappingError.withMessage(
                            400, String.format("Can not use query parameters with %s", url)));
        }

        RepositoryQueryResult queryResults;
        boolean allowFiltering =
                runtime.apiConfig().forParams().willAllowFilteringThroughUrlParams();
        QueryFilterParams effectiveQueryParams =
                allowFiltering ? queryParams : new QueryFilterParams();

        ThingReadRequestMapping mapping =
                new ThingReadRequestMapper(runtime.schema())
                        .map(new ThingRouteMapper(runtime.schema()).map(url), effectiveQueryParams);
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        queryResults = runtime.queryService().execute(mapping.getQuery(), context.store());

        return apiMapper.map(url, queryResults);
    }
}
