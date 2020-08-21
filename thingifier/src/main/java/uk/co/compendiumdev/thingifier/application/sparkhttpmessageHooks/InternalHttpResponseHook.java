package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponse;

public interface InternalHttpResponseHook {
    // throw an exception to return the response
    // amend the response in hook
    void run(HttpApiRequest request, InternalHttpResponse response);
}
