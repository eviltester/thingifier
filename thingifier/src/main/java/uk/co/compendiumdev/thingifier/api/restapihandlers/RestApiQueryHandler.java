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
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

public final class RestApiQueryHandler {

    private final ThingifierApiRuntime runtime;

    public RestApiQueryHandler(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
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

        EffectiveQueryParams effectiveQueryParams =
                EffectiveQueryParams.forQuery(runtime.apiConfig(), route, queryParams);
        if (effectiveQueryParams.isError()) {
            return apiMapper.map(effectiveQueryParams.error());
        }

        ThingReadRequestMapping mapping =
                new ThingReadRequestMapper(runtime.schema())
                        .map(route, effectiveQueryParams.queryParams());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        RepositoryQueryResult queryResults =
                runtime.queryService().execute(mapping.getQuery(), context.store(), false);
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
                .ruleFor(RoutingVerb.QUERY, url, runtime.apiConfig().getApiEndPointPrefix())
                .map(rule -> rule.responseEntityViewFor(response.getStatusCode()))
                .filter(entity::hasViewNamed)
                .map(entity::getViewNamed)
                .orElse(null);
    }

    private ApiResponse advertiseQuery(final ApiResponse response) {
        return response.setHeader(
                ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
    }
}
