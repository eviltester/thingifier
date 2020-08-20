package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public interface HttpApiResponseHook {
    // throw an exception to return the response
    // amend the response in hook
    void run(HttpApiRequest request, HttpApiResponse response);
}
