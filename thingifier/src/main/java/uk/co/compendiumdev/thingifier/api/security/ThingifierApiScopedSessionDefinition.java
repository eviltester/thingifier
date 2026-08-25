package uk.co.compendiumdev.thingifier.api.security;

import java.util.Optional;
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
    private boolean anonymousScopeForReads;
    private boolean anonymousDefaultScopeForReads;
    private ThingifierApiAnonymousDataScopeResolver anonymousReadDataScopeResolver;
    private boolean anonymousScopeForWrites;
    private ThingifierApiAnonymousDataScopeResolver anonymousWriteDataScopeResolver;
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
        this.anonymousScopeForReads = true;
        this.anonymousDefaultScopeForReads = true;
        this.anonymousReadDataScopeResolver =
                context -> ThingifierApiDataScopeSelection.defaultDataScope();
        return this;
    }

    /**
     * Allows read-style generated routes to use a named data scope when no credential is supplied.
     *
     * <p>The data scope is selected by trusted application configuration, not by the incoming
     * request. If a credential is supplied, Thingifier still validates it and invalid credentials
     * reject in v1.
     *
     * @param dataScopeName anonymous read data scope
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousReadsUsingDataScope(
            final String dataScopeName) {
        return allowAnonymousReadsUsingDataScope(
                dataScopeName, DataScopeCreationPolicy.USE_EXISTING_ONLY);
    }

    /**
     * Allows read-style generated routes to use a named data scope when no credential is supplied.
     *
     * @param dataScopeName anonymous read data scope
     * @param creationPolicy policy used when the anonymous scope does not exist
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousReadsUsingDataScope(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        final ThingifierApiDataScopeSelection selection =
                ThingifierApiDataScopeSelection.useDataScope(dataScopeName, creationPolicy);
        return allowAnonymousReadsUsingDataScope(context -> selection);
    }

    /**
     * Allows read-style generated routes to resolve the anonymous data scope dynamically.
     *
     * <p>The resolver runs only when the scoped-session credential is missing and anonymous read
     * access is allowed for the route. It is intended for application-owned decisions such as
     * choosing a public tenant, demo workspace, or single-player data scope from server-side state.
     *
     * @param resolver trusted anonymous data-scope resolver
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousReadsUsingDataScope(
            final ThingifierApiAnonymousDataScopeResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("anonymous data-scope resolver is required");
        }
        this.anonymousScopeForReads = true;
        this.anonymousDefaultScopeForReads = false;
        this.anonymousReadDataScopeResolver = resolver;
        return this;
    }

    /**
     * Allows write-style generated routes to use a named data scope when no credential is supplied.
     *
     * <p>This is intentionally opt-in for applications that offer public, demo, or single-user
     * mutable state. If a credential is supplied, Thingifier still validates it and invalid
     * credentials reject rather than falling back to the anonymous write scope.
     *
     * @param dataScopeName anonymous write data scope
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousWritesUsingDataScope(
            final String dataScopeName) {
        return allowAnonymousWritesUsingDataScope(
                dataScopeName, DataScopeCreationPolicy.USE_EXISTING_ONLY);
    }

    /**
     * Allows write-style generated routes to use a named data scope when no credential is supplied.
     *
     * @param dataScopeName anonymous write data scope
     * @param creationPolicy policy used when the anonymous scope does not exist
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousWritesUsingDataScope(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        final ThingifierApiDataScopeSelection selection =
                ThingifierApiDataScopeSelection.useDataScope(dataScopeName, creationPolicy);
        return allowAnonymousWritesUsingDataScope(context -> selection);
    }

    /**
     * Allows write-style generated routes to resolve the anonymous data scope dynamically.
     *
     * <p>The resolver runs only when the scoped-session credential is missing and anonymous write
     * access is allowed for the route. Configuring anonymous writes clears the authenticated-write
     * requirement so fluent write policy calls have predictable last-call-wins behaviour.
     *
     * @param resolver trusted anonymous data-scope resolver
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition allowAnonymousWritesUsingDataScope(
            final ThingifierApiAnonymousDataScopeResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("anonymous write data-scope resolver is required");
        }
        this.anonymousScopeForWrites = true;
        this.anonymousWriteDataScopeResolver = resolver;
        this.authenticatedScopeForWrites = false;
        return this;
    }

    /**
     * Requires write-style generated routes to have a valid scoped-session credential.
     *
     * <p>Configuring required writes clears any anonymous write scope so fluent write policy calls
     * have predictable last-call-wins behaviour.
     *
     * @return this definition for fluent configuration
     */
    public ThingifierApiScopedSessionDefinition requireAuthenticatedScopeForWrites() {
        this.authenticatedScopeForWrites = true;
        this.anonymousScopeForWrites = false;
        this.anonymousWriteDataScopeResolver = null;
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
     * @return true when read-style routes may fall back to an anonymous scope
     */
    public boolean allowsAnonymousScopeForReads() {
        return anonymousScopeForReads;
    }

    /**
     * @return true when read-style routes may fall back specifically to the default scope
     */
    public boolean allowsAnonymousDefaultScopeForReads() {
        return anonymousDefaultScopeForReads;
    }

    /**
     * Returns the trusted resolver used for missing-credential anonymous reads.
     *
     * @return resolver when anonymous reads are configured
     */
    public Optional<ThingifierApiAnonymousDataScopeResolver> anonymousDataScopeResolver() {
        return anonymousReadDataScopeResolver();
    }

    /**
     * Returns the trusted resolver used for missing-credential anonymous reads.
     *
     * <p>This named read accessor keeps the read and write anonymous scope policies explicit while
     * {@link #anonymousDataScopeResolver()} remains as the original read-scope alias.
     *
     * @return resolver when anonymous reads are configured
     */
    public Optional<ThingifierApiAnonymousDataScopeResolver> anonymousReadDataScopeResolver() {
        return Optional.ofNullable(anonymousReadDataScopeResolver);
    }

    /**
     * Selects the anonymous data scope for a missing-credential read request.
     *
     * @param context route and request context
     * @return selected data scope, or null if the resolver is absent or returns null
     */
    public ThingifierApiDataScopeSelection anonymousDataScopeSelection(
            final ThingifierApiScopedSessionContext context) {
        return anonymousReadDataScopeSelection(context);
    }

    /**
     * Selects the anonymous data scope for a missing-credential read request.
     *
     * @param context route and request context
     * @return selected data scope, or null if the resolver is absent or returns null
     */
    public ThingifierApiDataScopeSelection anonymousReadDataScopeSelection(
            final ThingifierApiScopedSessionContext context) {
        if (anonymousReadDataScopeResolver == null) {
            return null;
        }
        return anonymousReadDataScopeResolver.selectDataScope(context);
    }

    /**
     * @return true when write-style routes may fall back to an anonymous scope
     */
    public boolean allowsAnonymousScopeForWrites() {
        return anonymousScopeForWrites;
    }

    /**
     * Returns the trusted resolver used for missing-credential anonymous writes.
     *
     * @return resolver when anonymous writes are configured
     */
    public Optional<ThingifierApiAnonymousDataScopeResolver> anonymousWriteDataScopeResolver() {
        return Optional.ofNullable(anonymousWriteDataScopeResolver);
    }

    /**
     * Selects the anonymous data scope for a missing-credential write request.
     *
     * @param context route and request context
     * @return selected data scope, or null if the resolver is absent or returns null
     */
    public ThingifierApiDataScopeSelection anonymousWriteDataScopeSelection(
            final ThingifierApiScopedSessionContext context) {
        if (anonymousWriteDataScopeResolver == null) {
            return null;
        }
        return anonymousWriteDataScopeResolver.selectDataScope(context);
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
