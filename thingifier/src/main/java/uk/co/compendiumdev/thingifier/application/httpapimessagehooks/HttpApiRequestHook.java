package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public interface HttpApiRequestHook {

    // todo: I'm not really comfortable with passing in the config here, reconsider this
    // return a response if you want the request processing
    // interupted and just return a response
    HttpApiResponse run(HttpApiRequest request, ThingifierApiConfig config);
}
