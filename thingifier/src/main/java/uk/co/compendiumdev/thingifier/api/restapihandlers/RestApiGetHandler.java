package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;

public class RestApiGetHandler {
    private final Thingifier thingifier;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final QueryFilterParams queryParams, final HttpHeadersBlock requestHeaders) {

        // if there are params, and we are not allowed to filter, and we enforce that
        if(queryParams.size()>0 &&
            thingifier.apiConfig().forParams().willEnforceFilteringThroughUrlParams() &&
            !thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){
            return ApiResponse.error(400,
                        String.format("Can not use query parameters with %s", url));
        }

        String instanceDatabaseName = SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        SimpleQuery queryResults;

        if(thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){
           queryResults = new SimpleQuery(thingifier.getERmodel().getSchema(), thingifier.getERmodel().getInstanceData(instanceDatabaseName), url).performQuery(
                   queryParams);
        }else{
            queryResults = new SimpleQuery(thingifier.getERmodel().getSchema(), thingifier.getERmodel().getInstanceData(instanceDatabaseName), url).performQuery();
        }

        // TODO: we should support pagination through query params
        // TODO: api config should also support defining sorting for specific end points
        List<EntityInstance> queryItems = queryResults.getListEntityInstances();


        // return a 404 if it doesn't match anything
        if (queryResults.lastMatchWasNothing() ||
                (queryResults.lastMatchWasInstance() && queryItems.isEmpty())) {
            // if query list was empty then return a 404
            return ApiResponse.error404(String.format("Could not find an instance with %s", url));
        }

        if (queryResults.lastMatchWasInstance()) {

            boolean asCollection = false;
//            if(queryResults.wasQueryIntendedToMatchAnInstance() && !thingifier.apiConfig().willReturnSingleGetItemsAsCollection()){
//                asCollection = false;
//            }

            if(queryResults.wasQueryIntendedToMatchAnInstance() && thingifier.apiConfig().willReturnSingleGetItemsAsCollection()){
                asCollection = true;
            }

            if (queryResults.isResultACollection() && !queryResults.wasQueryIntendedToMatchAnInstance()) {
                asCollection = true;
            }

            if(asCollection){
                // if we asked for /projects then we should always return a collection
                return ApiResponse.success().
                        returnInstanceCollection(
                                queryResults.getListEntityInstances());
            }else {
                return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance());
            }

        } else {

            return ApiResponse.success().
                    returnInstanceCollection(queryItems).
                    resultContainsType(queryResults.resultContainsDefn());
        }
    }

}
