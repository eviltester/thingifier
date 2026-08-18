package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked after Thingifier validation and before action execution.
 *
 * <p>This phase lets callers convert validation outcomes into custom behavior, reject otherwise
 * valid work, or clear a recoverable validation failure before action execution.
 */
public interface AfterValidationHook {

    /**
     * Runs the hook against the after-validation context.
     *
     * @param context lifecycle context for the current request
     */
    void run(AfterValidationContext context);
}
