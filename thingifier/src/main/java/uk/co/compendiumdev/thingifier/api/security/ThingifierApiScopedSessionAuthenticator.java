package uk.co.compendiumdev.thingifier.api.security;

/**
 * Validates one scoped-session credential and decides the trusted request scope.
 *
 * <p>Thingifier calls this only when the configured credential source is present. A returned
 * authenticated result may select a data scope; an unauthenticated result means the presented
 * credential is invalid and must reject in v1 rather than falling back to anonymous/default data.
 */
@FunctionalInterface
public interface ThingifierApiScopedSessionAuthenticator {

    /**
     * Authenticates the presented session credential.
     *
     * @param context immutable request and credential context
     * @return authentication result controlling principal, rejection, and optional data scope
     */
    ThingifierApiScopedSessionResult authenticate(ThingifierApiScopedSessionContext context);
}
