package uk.co.compendiumdev.thingifier.application;

import spark.Request;
import spark.Response;

public interface PreRequestHook {
    // return false if we want to 'stop' the request
    boolean run(Request request, Response response);
}
