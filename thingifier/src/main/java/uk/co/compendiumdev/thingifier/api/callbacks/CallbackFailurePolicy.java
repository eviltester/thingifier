package uk.co.compendiumdev.thingifier.api.callbacks;

/**
 * Controls how Thingifier reacts when a route operation callback throws.
 *
 * <p>Callbacks are trusted application code running after Thingifier has decided an operation
 * result. Applications can choose whether a side-effect failure should fail the visible API request
 * or be logged while preserving the original response.
 */
public enum CallbackFailurePolicy {
    /**
     * Convert the callback exception into a 500 API response.
     *
     * <p>This is the default because silently skipping application side effects can leave
     * application-owned state inconsistent with Thingifier-managed data.
     */
    FAIL_REQUEST,

    /**
     * Log the callback exception and preserve the original operation response.
     *
     * <p>Use this when the callback is observational, such as diagnostics or best-effort metrics,
     * and the API operation should not fail because the callback failed.
     */
    LOG_AND_CONTINUE
}
