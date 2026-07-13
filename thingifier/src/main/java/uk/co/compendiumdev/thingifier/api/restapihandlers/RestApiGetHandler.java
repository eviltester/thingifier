package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingQueryService;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

public class RestApiGetHandler {
    private final Thingifier thingifier;
    private final ThingQueryService queryService;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
        this.queryService = new ThingQueryService();
    }

    public ApiResponse handle(
            final String url,
            final QueryFilterParams queryParams,
            final HttpHeadersBlock requestHeaders) {

        // if there are params, and we are not allowed to filter, and we enforce that
        if (queryParams.size() > 0
                && thingifier.apiConfig().forParams().willEnforceFilteringThroughUrlParams()
                && !thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()) {
            return ApiResponse.error(
                    400, String.format("Can not use query parameters with %s", url));
        }

        String instanceDatabaseName =
                SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        RepositoryQueryResult queryResults;
        boolean allowFiltering =
                thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams();
        QueryFilterParams effectiveQueryParams =
                allowFiltering ? queryParams : new QueryFilterParams();

        ThingReadRequestMapping mapping =
                new ThingReadRequestMapper(thingifier).map(url, effectiveQueryParams);
        if (mapping.isError()) {
            return mapping.getErrorResponse();
        }

        queryResults =
                queryService.execute(mapping.getQuery(), thingifier.getStore(instanceDatabaseName));

        return new ThingReadResultApiMapper(thingifier.apiConfig()).map(url, queryResults);
    }
}
