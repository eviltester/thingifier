package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked after request mapping and before Thingifier validation.
 *
 * <p>This is the right phase to replace a mapped command or query when validation should evaluate
 * the replacement rather than the original request.
 */
public interface BeforeValidationHook {

    /**
     * Runs the hook against the before-validation context.
     *
     * @param context lifecycle context for the current request
     */
    void run(BeforeValidationContext context);
}
