package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public interface InternalHttpRequestHook {
    // return an HttpApiResponse if you want to end immediately from the hook
    HttpApiResponse run(HttpApiRequest request);
}
