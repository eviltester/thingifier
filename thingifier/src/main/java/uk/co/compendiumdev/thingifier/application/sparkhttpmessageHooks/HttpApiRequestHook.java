package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

public interface HttpApiRequestHook {
    // return an HttpApiResponse if you want to end immediately from the hook
    HttpApiResponse run(HttpApiRequest request);
}
