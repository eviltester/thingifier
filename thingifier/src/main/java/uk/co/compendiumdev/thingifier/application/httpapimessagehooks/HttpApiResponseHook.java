package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public interface HttpApiResponseHook {

    // return a response if you want this to be
    // the final response and not process any further hooks
    HttpApiResponse run(HttpApiRequest request, HttpApiResponse response, ThingifierApiConfig config);

}
