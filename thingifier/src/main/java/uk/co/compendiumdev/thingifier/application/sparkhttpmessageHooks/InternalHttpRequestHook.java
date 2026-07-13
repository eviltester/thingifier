package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.application.internalhttp.InternalHttpResponse;

public interface InternalHttpRequestHook {
    // return an InternalHttpResponse if you want to end immediately from the hook
    InternalHttpResponse run(InternalHttpRequest request);
}
