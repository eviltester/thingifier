package uk.co.compendiumdev.thingifier.api.security;

import java.util.Optional;

/**
 * Resolved scoped-session policy for one request route.
 *
 * <p>Route rules and contract-level read/write shortcuts are stored separately in the API spec.
 * This object gives runtime handling one simple decision: optional anonymous scope or required
 * authenticated scoped session, plus the definition that should resolve credentials.
 */
public final class ThingifierApiScopedSessionPolicy {

    /** How a matched route should treat missing scoped-session credentials. */
    public enum Mode {
        /** Missing credentials use the default scope, while invalid supplied credentials reject. */
        ALLOW_ANONYMOUS_DEFAULT_SCOPE,

        /**
         * Missing credentials use the anonymous data-scope resolver configured on the definition.
         */
        ALLOW_ANONYMOUS_CONFIGURED_SCOPE,

        /** Missing or invalid credentials reject before validators and handlers run. */
        REQUIRE_AUTHENTICATED_SCOPE
    }

    private final Mode mode;
    private final String sessionName;
    private final ThingifierApiScopedSessionDefinition definition;

    private ThingifierApiScopedSessionPolicy(
            final Mode mode,
            final String sessionName,
            final ThingifierApiScopedSessionDefinition definition) {
        this.mode = mode;
        this.sessionName = sessionName;
        this.definition = definition;
    }

    /**
     * Creates a policy backed by a configured scoped-session definition.
     *
     * @param definition definition used to resolve credentials
     * @param mode missing-credential behaviour
     * @return route scoped-session policy
     */
    public static ThingifierApiScopedSessionPolicy configured(
            final ThingifierApiScopedSessionDefinition definition, final Mode mode) {
        if (definition == null) {
            throw new IllegalArgumentException("scoped-session definition is required");
        }
        if (mode == null) {
            throw new IllegalArgumentException("scoped-session mode is required");
        }
        return new ThingifierApiScopedSessionPolicy(mode, definition.name(), definition);
    }

    /**
     * Creates a policy that points at a missing definition.
     *
     * <p>Runtime returns a configuration error instead of silently allowing a route that was
     * declared to require scoped-session handling.
     *
     * @param sessionName requested scoped-session name
     * @param mode missing-credential behaviour requested by the route
     * @return unresolved route scoped-session policy
     */
    public static ThingifierApiScopedSessionPolicy unresolved(
            final String sessionName, final Mode mode) {
        return new ThingifierApiScopedSessionPolicy(
                mode, SecuritySchemeNames.requireValid(sessionName), null);
    }

    /**
     * @return missing-credential behaviour for the route
     */
    public Mode mode() {
        return mode;
    }

    /**
     * @return scoped-session name used for principal lookup
     */
    public String sessionName() {
        return sessionName;
    }

    /**
     * @return configured definition, or empty when the route references a missing definition
     */
    public Optional<ThingifierApiScopedSessionDefinition> definition() {
        return Optional.ofNullable(definition);
    }

    /**
     * @return true when missing credentials should use the default scope
     */
    public boolean allowsAnonymousDefaultScope() {
        return mode == Mode.ALLOW_ANONYMOUS_DEFAULT_SCOPE;
    }

    /**
     * @return true when missing credentials should use the definition's anonymous scope resolver
     */
    public boolean allowsAnonymousConfiguredScope() {
        return mode == Mode.ALLOW_ANONYMOUS_CONFIGURED_SCOPE;
    }

    /**
     * @return true when missing credentials should continue anonymously
     */
    public boolean allowsAnonymousScope() {
        return allowsAnonymousDefaultScope() || allowsAnonymousConfiguredScope();
    }

    /**
     * @return true when missing credentials should reject the request
     */
    public boolean requiresAuthenticatedScope() {
        return mode == Mode.REQUIRE_AUTHENTICATED_SCOPE;
    }
}
