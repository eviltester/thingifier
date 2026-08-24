package uk.co.compendiumdev.thingifier.api.security;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Defines a named scoped-session resolver for an API contract.
 *
 * <p>A scoped session is for credentials such as session ids, challenge ids, tenant tokens, or
 * other application-owned values that need validation before they may select a Thingifier data
 * scope. It is not a direct "database from header" mapping.
 */
public final class ThingifierApiScopedSessionDefinition {

    private final String name;
    private ThingifierApiScopedSessionCredentialSourceType credentialSourceType;
    private String credentialSourceName;
    private ThingifierApiScopedSessionAuthenticator authenticator;
    private boolean anonymousDefaultScopeForReads;
    private boolean authenticatedScopeForWrites;
    private int missingCredentialStatusCode;
    private String missingCredentialMessage;
    private int invalidCredentialStatusCode;
    private String invalidCredentialMessage;

    /**
     * Creates a named scoped-session definition.
     *
     * @param name public scoped-session name used by route rules and principal lookup
     */
    public ThingifierApiScopedSessionDefinition(final String name) {
        this.name = SecuritySchemeNames.requireValid(name);
        this.missingCredentialStatusCode = 401;
        this.missingCredentialMessage = "Unauthorized";
        this.invalidCredentialStatusCode = 401;
        this.invalidCredentialMessage = "Unauthorized";
    }

    /**
     * Reads the session credential from an HTTP header.
     *
     * @param headerName request header carrying the scoped-session credential
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition fromHeader(final String headerName) {
        this.credentialSourceType = ThingifierApiScopedSessionCredentialSourceType.HEADER;
        this.credentialSourceName = SecuritySchemeNames.requireValidHeaderName(headerName);
        return this;
    }

    /**
     * Reads the session credential from a URL query parameter.
     *
     * @param queryParamName query parameter carrying the scoped-session credential
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition fromQueryParam(final String queryParamName) {
        this.credentialSourceType = ThingifierApiScopedSessionCredentialSourceType.QUERY_PARAM;
        this.credentialSourceName = requireName(queryParamName, "query parameter name");
        return this;
    }

    /**
     * Reads the session credential from a named cookie.
     *
     * <p>The HTTP adapter reads cookies from the normal {@code Cookie} request header, keeping the
     * public API independent of a particular server framework.
     *
     * @param cookieName cookie carrying the scoped-session credential
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition fromCookie(final String cookieName) {
        this.credentialSourceType = ThingifierApiScopedSessionCredentialSourceType.COOKIE;
        this.credentialSourceName = requireName(cookieName, "cookie name");
        return this;
    }

    /**
     * Registers the trusted resolver for this session credential.
     *
     * @param authenticator callback that validates the credential and may select a data scope
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition authenticateWith(
            final ThingifierApiScopedSessionAuthenticator authenticator) {
        if (authenticator == null) {
            throw new IllegalArgumentException("scoped-session authenticator is required");
        }
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Allows read-style generated routes to use the default data scope when no credential is
     * supplied.
     *
     * <p>If a credential is present, Thingifier validates it. Invalid credentials reject in v1 even
     * for anonymous-readable routes.
     *
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousDefaultScopeForReads() {
        this.anonymousDefaultScopeForReads = true;
        return this;
    }

    /**
     * Requires write-style generated routes to have a valid scoped-session credential.
     *
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition requireAuthenticatedScopeForWrites() {
        this.authenticatedScopeForWrites = true;
        return this;
    }

    /**
     * Configures the response used when a route requires a scoped session and no credential is
     * supplied.
     *
     * @param statusCode response status code
     * @param message response error message
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition onMissingRequiredCredential(
            final int statusCode, final String message) {
        this.missingCredentialStatusCode = statusCode;
        this.missingCredentialMessage = message == null ? "" : message;
        return this;
    }

    /**
     * Configures the response used when a resolver rejects a supplied credential as invalid.
     *
     * @param statusCode response status code
     * @param message response error message
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition onInvalidCredential(
            final int statusCode, final String message) {
        this.invalidCredentialStatusCode = statusCode;
        this.invalidCredentialMessage = message == null ? "" : message;
        return this;
    }

    /**
     * @return scoped-session definition name
     */
    public String name() {
        return name;
    }

    /**
     * @return configured credential source type, or null when no source is configured
     */
    public ThingifierApiScopedSessionCredentialSourceType credentialSourceType() {
        return credentialSourceType;
    }

    /**
     * @return configured credential source name, or null when no source is configured
     */
    public String credentialSourceName() {
        return credentialSourceName;
    }

    /**
     * @return configured resolver, or null when none has been registered
     */
    public ThingifierApiScopedSessionAuthenticator authenticator() {
        return authenticator;
    }

    /**
     * @return true when read-style routes may fall back to the default scope
     */
    public boolean allowsAnonymousDefaultScopeForReads() {
        return anonymousDefaultScopeForReads;
    }

    /**
     * @return true when write-style routes require a valid scoped session
     */
    public boolean requiresAuthenticatedScopeForWrites() {
        return authenticatedScopeForWrites;
    }

    /**
     * @return configured missing-credential response
     */
    public ApiResponse missingRequiredCredentialResponse() {
        return ApiResponse.error(missingCredentialStatusCode, missingCredentialMessage);
    }

    /**
     * @return configured invalid-credential response
     */
    public ApiResponse invalidCredentialResponse() {
        return ApiResponse.error(invalidCredentialStatusCode, invalidCredentialMessage);
    }

    /**
     * Reports whether the definition has enough information to read a credential.
     *
     * @return true when a credential source type and name have been configured
     */
    public boolean hasCredentialSource() {
        return credentialSourceType != null
                && credentialSourceName != null
                && !credentialSourceName.trim().isEmpty();
    }

    private String requireName(final String value, final String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
