package uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks;

import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public interface InternalHttpRequestHook {
    // return an InternalHttpResponse if you want to end immediately from the hook
    InternalHttpResponse run(InternalHttpRequest request);
}
