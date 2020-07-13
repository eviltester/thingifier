package uk.co.compendiumdev.thingifier.application.httpapimessagehooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public interface HttpApiResponseHook {

    // return a response if you want this to be
    // the final response and not process any further hooks
    HttpApiResponse run(HttpApiResponse response);

}
