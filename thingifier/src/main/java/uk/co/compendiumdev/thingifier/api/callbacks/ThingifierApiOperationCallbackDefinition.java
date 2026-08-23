package uk.co.compendiumdev.thingifier.api.callbacks;

import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;

/**
 * Runtime-only registration for one route operation callback.
 *
 * <p>The definition is code-only by design. Java callbacks cannot safely round-trip through YAML or
 * OpenAPI, so Thingifier stores them only in the in-memory API contract and uses the name for
 * diagnostics.
 */
public final class ThingifierApiOperationCallbackDefinition {

    /** Outcome selector used when deciding whether a callback should run. */
    public enum Outcome {
        /** Run for any completed outcome. */
        ANY,

        /** Run only for 2xx/3xx operation responses. */
        SUCCESS,

        /** Run only for non-success operation responses. */
        FAILURE,

        /** Run only when the final status code matches {@link #statusCode()}. */
        STATUS
    }

    private final ThingifierApiRouteRule routeRule;
    private final String name;
    private final Outcome outcome;
    private final Integer statusCode;
    private final ThingifierApiOperationCallback callback;
    private CallbackFailurePolicy failurePolicy;

    /**
     * Creates a callback registration.
     *
     * @param routeRule route that owns the callback
     * @param name stable diagnostic name
     * @param outcome outcome selector
     * @param statusCode status code for {@link Outcome#STATUS}, otherwise null
     * @param callback trusted application callback
     */
    public ThingifierApiOperationCallbackDefinition(
            final ThingifierApiRouteRule routeRule,
            final String name,
            final Outcome outcome,
            final Integer statusCode,
            final ThingifierApiOperationCallback callback) {
        if (routeRule == null) {
            throw new IllegalArgumentException("route rule is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("callback name is required");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("callback outcome is required");
        }
        if (outcome == Outcome.STATUS && statusCode == null) {
            throw new IllegalArgumentException("status callback requires a status code");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback is required");
        }
        this.routeRule = routeRule;
        this.name = name.trim();
        this.outcome = outcome;
        this.statusCode = statusCode;
        this.callback = callback;
        this.failurePolicy = CallbackFailurePolicy.FAIL_REQUEST;
    }

    /**
     * Sets the failure policy for this callback.
     *
     * @param policy callback exception handling policy
     * @return owning route rule so route configuration can continue fluently
     */
    public ThingifierApiRouteRule onCallbackFailure(final CallbackFailurePolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("callback failure policy is required");
        }
        this.failurePolicy = policy;
        return routeRule;
    }

    /**
     * Returns the stable diagnostic callback name.
     *
     * @return callback name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the outcome selector for this callback.
     *
     * @return configured outcome selector
     */
    public Outcome outcome() {
        return outcome;
    }

    /**
     * Returns the status code matched by status-specific callbacks.
     *
     * @return status code, or null for non-status callbacks
     */
    public Integer statusCode() {
        return statusCode;
    }

    /**
     * Returns the application callback.
     *
     * @return trusted callback
     */
    public ThingifierApiOperationCallback callback() {
        return callback;
    }

    /**
     * Returns the configured callback failure policy.
     *
     * @return failure policy
     */
    public CallbackFailurePolicy failurePolicy() {
        return failurePolicy;
    }

    /**
     * Reports whether this callback should run for the supplied result.
     *
     * @param result operation result
     * @return true when the outcome selector matches
     */
    public boolean matches(final ThingifierApiOperationResult result) {
        if (result == null) {
            return false;
        }
        switch (outcome) {
            case ANY:
                return true;
            case SUCCESS:
                return result.successful();
            case FAILURE:
                return result.failed();
            case STATUS:
                return statusCode != null && statusCode == result.statusCode();
            default:
                return false;
        }
    }
}
