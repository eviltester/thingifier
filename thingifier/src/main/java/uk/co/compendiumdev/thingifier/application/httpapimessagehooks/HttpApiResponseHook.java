package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

/**
 * An HttpApiResponseHook will run after all API processing has taken place.
 * Suitable for making any changes or checks on the generated HttpApiResponse.
 * Any hook which returns an HttpApiResponse will shortcut all processing and
 * the returned HttpApiResponse will be used to return to the user.
 */
public interface HttpApiResponseHook {

    // return a response if you want this to be
    // the final response and not process any further hooks
    HttpApiResponse run(HttpApiRequest request, HttpApiResponse response, ThingifierApiConfig config);

}
