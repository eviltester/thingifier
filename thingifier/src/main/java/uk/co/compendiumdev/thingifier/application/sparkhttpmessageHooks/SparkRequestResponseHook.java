package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import spark.Request;
import spark.Response;

/**
 * SparkRequestResponseHook (s) are run prior to any processing.
 * They work directly on the Spark request and Spark responses
 */
public interface SparkRequestResponseHook {
    // throw an exception if we want to 'stop' the request and return the response
    void run(Request request, Response response);
}
