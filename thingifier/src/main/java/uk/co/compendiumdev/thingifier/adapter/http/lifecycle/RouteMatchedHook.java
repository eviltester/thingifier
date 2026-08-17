package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked once a dynamic Thingifier API request has been matched to a route.
 *
 * <p>This phase is useful for route-aware policy decisions that should happen before body parsing
 * or validation, including early short-circuit responses.
 */
public interface RouteMatchedHook {

    /**
     * Runs the hook against the route-matched context.
     *
     * @param context lifecycle context for the current request
     */
    void run(RouteMatchedContext context);
}
