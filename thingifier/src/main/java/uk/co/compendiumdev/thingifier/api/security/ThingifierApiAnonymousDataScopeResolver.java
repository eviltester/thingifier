package uk.co.compendiumdev.thingifier.api.security;

/**
 * Chooses the data scope for a missing scoped-session credential.
 *
 * <p>This callback is trusted server-side configuration, not a request-controlled database mapper.
 * Thingifier calls it only after route matching has determined that anonymous access is allowed for
 * the current operation, and before validators, authorizers, hooks, handlers, and response
 * rendering run.
 */
@FunctionalInterface
public interface ThingifierApiAnonymousDataScopeResolver {

    /**
     * Selects the data scope to use for an anonymous request.
     *
     * @param context immutable route and request context for the missing credential request
     * @return trusted data-scope selection, or null to signal a configuration error
     */
    ThingifierApiDataScopeSelection selectDataScope(ThingifierApiScopedSessionContext context);
}
