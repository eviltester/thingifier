package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;

import java.util.List;

public class RestApiGetHandler {
    private final Thingifier thingifier;

    public RestApiGetHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url) {
        SimpleQuery queryResults = new SimpleQuery(thingifier, url).performQuery();
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
                return ApiResponse.success().returnInstanceCollection(queryResults.getListThingInstance());
            } else {
                return ApiResponse.success().returnSingleInstance(queryResults.getLastInstance());
            }
        } else {

            return ApiResponse.success().returnInstanceCollection(queryItems).resultContainsType(queryResults.resultContainsDefn());
        }
    }
}
