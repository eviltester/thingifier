package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public interface HttpApiRequestHook {

    // return a response if you want the request processing
    // interupted and just return a response
    HttpApiResponse run(HttpApiRequest request);
}
