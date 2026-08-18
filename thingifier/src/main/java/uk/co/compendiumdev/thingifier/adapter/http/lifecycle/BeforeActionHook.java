package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked after validation and immediately before the read or write action.
 *
 * <p>This phase is intended for last-mile command or query replacement when validation has already
 * accepted the work that will be executed.
 */
public interface BeforeActionHook {

    /**
     * Runs the hook against the before-action context.
     *
     * @param context lifecycle context for the current request
     */
    void run(BeforeActionContext context);
}
