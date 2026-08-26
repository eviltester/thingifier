package uk.co.compendiumdev.thingifier.api.callbacks;

/**
 * Runtime-only registration for one route final-response callback.
 *
 * <p>The definition is code-only by design. Java callbacks cannot safely round-trip through YAML or
 * OpenAPI, so Thingifier stores them only in the in-memory API contract and uses the name for
 * diagnostics when a callback throws.
 */
public final class ThingifierApiResponseCallbackDefinition {

    private final String name;
    private final ThingifierApiResponseCallback callback;

    /**
     * Creates a final-response callback registration.
     *
     * @param name stable diagnostic name
     * @param callback trusted application callback
     */
    public ThingifierApiResponseCallbackDefinition(
            final String name, final ThingifierApiResponseCallback callback) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("callback name is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback is required");
        }
        this.name = name.trim();
        this.callback = callback;
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
     * Returns the application callback.
     *
     * @return trusted callback
     */
    public ThingifierApiResponseCallback callback() {
        return callback;
    }
}
