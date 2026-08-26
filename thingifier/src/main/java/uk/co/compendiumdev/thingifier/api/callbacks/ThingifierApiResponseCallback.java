package uk.co.compendiumdev.thingifier.api.callbacks;

/**
 * Trusted application callback invoked after a route has produced its final HTTP response.
 *
 * <p>Use final response callbacks for observational side effects that need the negotiated HTTP
 * outcome, such as audit events, challenge completion, or response-based metrics. The callback is
 * read-only in v1; response mutation belongs in route response policies or legacy HTTP response
 * hooks.
 */
@FunctionalInterface
public interface ThingifierApiResponseCallback {

    /**
     * Runs the application callback for one final route response.
     *
     * @param context immutable route, request, auth, and data-scope information
     * @param response immutable final HTTP response details
     */
    void run(ThingifierApiOperationContext context, ThingifierApiFinalResponse response);
}
