package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

/**
 * Run prior to any validation or internal API processing
 * This can be used to amend the request if necessary
 * And we can shortcut any API processing by returning an HttpApiResponse
 * This response would be passed back to the user.
 */
public interface HttpApiRequestHook {

    // todo: I'm not really comfortable with passing in the full config here, reconsider this
    // return a response if you want the request processing
    // interupted and just return a response
    HttpApiResponse run(HttpApiRequest request, ThingifierApiConfig config);
}
