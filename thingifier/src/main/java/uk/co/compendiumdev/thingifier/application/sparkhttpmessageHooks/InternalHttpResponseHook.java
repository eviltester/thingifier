package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpResponse;

public interface InternalHttpResponseHook {
    // throw an exception to return the response
    // amend the response in hook
    void run(InternalHttpRequest request, InternalHttpResponse response);
}
