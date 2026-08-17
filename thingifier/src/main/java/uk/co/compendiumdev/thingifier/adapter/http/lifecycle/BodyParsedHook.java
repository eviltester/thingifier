package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Hook invoked after request body and query data are parsed.
 *
 * <p>This phase is designed for explicit request data changes before the request is mapped into a
 * Thingifier read query or write command.
 */
public interface BodyParsedHook {

    /**
     * Runs the hook against the parsed-body context.
     *
     * @param context lifecycle context for the current request
     */
    void run(BodyParsedContext context);
}
