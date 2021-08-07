package uk.co.compendiumdev.thingifier.application.routehandlers;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public interface HttpApiRequestHandler {

    ApiResponse handle(final HttpApiRequest myRequest);
}
