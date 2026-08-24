package uk.co.compendiumdev.thingifier.api.security;

/**
 * Identifies where a scoped-session credential is read from.
 *
 * <p>Scoped sessions are deliberately separate from the legacy Thingifier data-scope header:
 * request input is treated as a credential first, and only trusted application resolver code may
 * turn it into an active data scope.
 */
public enum ThingifierApiScopedSessionCredentialSourceType {
    /** Credential is read from an HTTP request header. */
    HEADER,

    /** Credential is read from a URL query parameter. */
    QUERY_PARAM,

    /** Credential is read from a named value in the Cookie request header. */
    COOKIE
}
