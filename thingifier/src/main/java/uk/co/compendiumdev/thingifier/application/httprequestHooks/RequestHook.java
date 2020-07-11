package uk.co.compendiumdev.thingifier.application.httprequestHooks;

import spark.Request;
import spark.Response;

public interface RequestHook {
    // throw an exception if we want to 'stop' the request
    void run(Request request, Response response);
}
