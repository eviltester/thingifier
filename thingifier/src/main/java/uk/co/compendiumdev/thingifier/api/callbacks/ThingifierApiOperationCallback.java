package uk.co.compendiumdev.thingifier.api.callbacks;

/**
 * Trusted application callback invoked after a route operation has produced an API result.
 *
 * <p>Use route operation callbacks for application side effects such as audit logging, projections,
 * cache invalidation, or synchronising app-owned state. Response shaping should stay in route
 * response policies or response hooks so callbacks can remain focused on observing the completed
 * operation.
 */
@FunctionalInterface
public interface ThingifierApiOperationCallback {

    /**
     * Runs the application callback for one completed route operation.
     *
     * @param context immutable route, request, auth, and data-scope information
     * @param result immutable operation outcome details
     */
    void run(ThingifierApiOperationContext context, ThingifierApiOperationResult result);
}
