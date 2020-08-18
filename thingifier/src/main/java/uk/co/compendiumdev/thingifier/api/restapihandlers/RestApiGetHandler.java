package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestApiGetHandler {
    private final Thingifier thingifier;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final Map<String, String> queryParams) {

        // if there are params, and we are not allowed to filter, and we enforce that
        if(queryParams.size()>0 &&
            thingifier.apiConfig().forParams().willEnforceFilteringThroughUrlParams() &&
            !thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){
            return ApiResponse.error(400,
                        String.format("Can not use query parameters with %s", url));
        }

        SimpleQuery queryResults;

        if(thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){
           queryResults = new SimpleQuery(thingifier.getERmodel(), url).performQuery(
                   queryParams);
        }else{
            queryResults = new SimpleQuery(thingifier.getERmodel(), url).performQuery();
        }

        List<ThingInstance> queryItems = queryResults.getListThingInstance();

        // return a 404 if it doesn't match anything
        if (queryResults.lastMatchWasNothing() ||
                (queryResults.lastMatchWasInstance() && queryItems.size() == 0)) {
            // if query list was empty then return a 404
            return ApiResponse.error404(String.format("Could not find an instance with %s", url));
        }

        if (queryResults.lastMatchWasInstance()) {
            if (queryResults.isResultACollection()) {
                // if we asked for /projects then we should always return a collection
                return ApiResponse.success().
                        returnInstanceCollection(
                                queryResults.getListThingInstance());
            } else {
                return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance());
            }
        } else {

            return ApiResponse.success().
                    returnInstanceCollection(queryItems).
                    resultContainsType(queryResults.resultContainsDefn());
        }
    }

    public ApiResponse handle(final String url) {
        return handle(url, new HashMap<>());
    }


}
