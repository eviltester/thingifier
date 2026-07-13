package uk.co.compendiumdev.thingifier.adapter.http.routehandlers;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public interface HttpApiRequestHandler {

    ApiResponse handle(HttpApiRequest myRequest);
}
