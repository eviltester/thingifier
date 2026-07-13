package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

public final class ThingReadResultApiMapper {

    private final ThingifierApiConfig apiConfig;

    public ThingReadResultApiMapper(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ApiResponse map(final ApiMappingError error) {
        return ApiResponse.error(error.statusCode(), error.messages());
    }

    public ApiResponse map(final String url, final RepositoryQueryResult queryResults) {
        List<EntityInstance> queryItems = queryResults.getListEntityInstances();

        if (queryResults.lastMatchWasNothing()
                || (queryResults.lastMatchWasInstance() && queryItems.isEmpty())) {
            return ApiResponse.error404(String.format("Could not find an instance with %s", url));
        }

        if (queryResults.lastMatchWasInstance()) {
            if (shouldReturnInstanceAsCollection(queryResults)) {
                return ApiResponse.success().returnInstanceCollection(queryItems);
            }
            return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance());
        }

        return ApiResponse.success()
                .returnInstanceCollection(queryItems)
                .resultContainsType(queryResults.resultContainsDefn());
    }

    private boolean shouldReturnInstanceAsCollection(final RepositoryQueryResult queryResults) {
        if (queryResults.wasQueryIntendedToMatchAnInstance()
                && apiConfig.willReturnSingleGetItemsAsCollection()) {
            return true;
        }

        return queryResults.isResultACollection()
                && !queryResults.wasQueryIntendedToMatchAnInstance();
    }
}
