package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked after the read or write action has produced its result and response.
 *
 * <p>This phase is intended for response shaping, result inspection, or explicit result replacement
 * after Thingifier has done the core work.
 */
public interface AfterActionHook {

    /**
     * Runs the hook against the after-action context.
     *
     * @param context lifecycle context for the current request
     */
    void run(AfterActionContext context);
}
