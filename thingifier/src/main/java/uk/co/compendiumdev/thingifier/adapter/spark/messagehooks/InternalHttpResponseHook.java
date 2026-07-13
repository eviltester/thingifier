package uk.co.compendiumdev.thingifier.adapter.spark.messagehooks;

import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public interface InternalHttpResponseHook {
    // throw an exception to return the response
    // amend the response in hook
    void run(InternalHttpRequest request, InternalHttpResponse response);
}
