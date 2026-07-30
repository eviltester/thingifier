package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiMappingError;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.query.PaginationParams;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

final class EffectiveQueryParams {

    private final QueryFilterParams queryParams;
    private final ApiMappingError error;

    private EffectiveQueryParams(final QueryFilterParams queryParams, final ApiMappingError error) {
        this.queryParams = queryParams;
        this.error = error;
    }

    static EffectiveQueryParams forGet(
            final ThingifierApiConfig apiConfig,
            final ThingRoute route,
            final QueryFilterParams requestedParams,
            final String url) {
        QueryFilterParams requested = paramsOrEmpty(requestedParams);
        boolean allowFiltering = apiConfig.forParams().willAllowFilteringThroughUrlParams();

        if (requested.fieldFilters().size() > 0
                && apiConfig.forParams().willEnforceFilteringThroughUrlParams()
                && !allowFiltering) {
            return error(
                    ApiMappingError.withMessage(
                            400, String.format("Can not use query parameters with %s", url)));
        }

        return from(apiConfig, route, requested, allowFiltering);
    }

    static EffectiveQueryParams forQuery(
            final ThingifierApiConfig apiConfig,
            final ThingRoute route,
            final QueryFilterParams requestedParams) {
        return from(apiConfig, route, paramsOrEmpty(requestedParams), true);
    }

    boolean isError() {
        return error != null;
    }

    ApiMappingError error() {
        return error;
    }

    QueryFilterParams queryParams() {
        return queryParams;
    }

    private static EffectiveQueryParams from(
            final ThingifierApiConfig apiConfig,
            final ThingRoute route,
            final QueryFilterParams requested,
            final boolean includeFilterAndSortControls) {
        QueryFilterParams effective = new QueryFilterParams();
        if (includeFilterAndSortControls) {
            effective.addAll(requested.withoutPagingParams());
        }

        if (!isPageable(route) || !apiConfig.forParams().willAllowPagingThroughUrlParams()) {
            return ok(effective);
        }

        PaginationParams pagination = new PaginationParams(requested);
        if (pagination.hasValidationError()) {
            return error(ApiMappingError.withMessage(400, pagination.validationError()));
        }

        int maxLimit = apiConfig.forParams().maxPagingLimit();
        int defaultLimit = Math.min(apiConfig.forParams().defaultPagingLimit(), maxLimit);
        int limit = Math.min(pagination.limitOr(defaultLimit), maxLimit);

        effective.put(PaginationParams.LIMIT_PARAMETER_NAME, Integer.toString(limit));
        effective.put(
                PaginationParams.OFFSET_PARAMETER_NAME, Integer.toString(pagination.offsetOr(0)));

        return ok(effective);
    }

    private static boolean isPageable(final ThingRoute route) {
        return route instanceof CollectionRoute || route instanceof RelationshipCollectionRoute;
    }

    private static QueryFilterParams paramsOrEmpty(final QueryFilterParams params) {
        return params == null ? new QueryFilterParams() : params;
    }

    private static EffectiveQueryParams ok(final QueryFilterParams queryParams) {
        return new EffectiveQueryParams(queryParams, null);
    }

    private static EffectiveQueryParams error(final ApiMappingError error) {
        return new EffectiveQueryParams(new QueryFilterParams(), error);
    }
}
