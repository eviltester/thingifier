package uk.co.compendiumdev.thingifier.api.security;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Result returned by a scoped-session resolver.
 *
 * <p>The result models the trust boundary for session-like credentials. A credential supplied by
 * the client is not a data-scope name; it becomes a data-scope decision only when application code
 * returns an authenticated result and selects a scope.
 */
public final class ThingifierApiScopedSessionResult {

    private final boolean authenticated;
    private final Object principal;
    private final ApiResponse rejectionResponse;
    private final boolean customRejectionResponse;
    private final ThingifierApiDataScopeSelection dataScopeSelection;

    private ThingifierApiScopedSessionResult(
            final boolean authenticated,
            final Object principal,
            final ApiResponse rejectionResponse,
            final boolean customRejectionResponse,
            final ThingifierApiDataScopeSelection dataScopeSelection) {
        this.authenticated = authenticated;
        this.principal = principal;
        this.rejectionResponse = rejectionResponse;
        this.customRejectionResponse = customRejectionResponse;
        this.dataScopeSelection = dataScopeSelection;
    }

    /**
     * Creates a successful scoped-session result.
     *
     * @param principal application principal, session record, user id, or other caller-owned object
     * @return successful scoped-session result
     */
    public static ThingifierApiScopedSessionResult authenticated(final Object principal) {
        return new ThingifierApiScopedSessionResult(true, principal, null, false, null);
    }

    /**
     * Creates a successful scoped-session result without a principal object.
     *
     * @return successful scoped-session result
     */
    public static ThingifierApiScopedSessionResult authenticated() {
        return authenticated(null);
    }

    /**
     * Creates an invalid-credential result.
     *
     * <p>Thingifier uses the scoped-session definition's invalid-credential response for this. It
     * is intentionally distinct from a missing credential, which Thingifier detects before calling
     * the resolver.
     *
     * @return invalid scoped-session result
     */
    public static ThingifierApiScopedSessionResult unauthenticated() {
        return new ThingifierApiScopedSessionResult(false, null, null, false, null);
    }

    /**
     * Creates a scoped-session rejection with a 401 status and message.
     *
     * @param message message to render in the response body
     * @return rejected scoped-session result
     */
    public static ThingifierApiScopedSessionResult rejected(final String message) {
        return rejected(401, message);
    }

    /**
     * Creates a scoped-session rejection with a status and message.
     *
     * @param status status code to return
     * @param message message to render in the response body
     * @return rejected scoped-session result
     */
    public static ThingifierApiScopedSessionResult rejected(
            final int status, final String message) {
        return new ThingifierApiScopedSessionResult(
                false, null, ApiResponse.error(status, message), false, null);
    }

    /**
     * Creates a scoped-session rejection with a complete response.
     *
     * <p>Use this when the application needs full control over the rejection response. Thingifier
     * returns it as supplied.
     *
     * @param response response to return instead of continuing request processing
     * @return rejected scoped-session result
     */
    public static ThingifierApiScopedSessionResult rejected(final ApiResponse response) {
        return new ThingifierApiScopedSessionResult(false, null, response, true, null);
    }

    /**
     * Selects a named data scope for the authenticated request.
     *
     * @param dataScopeName data scope chosen by trusted scoped-session resolver code
     * @return new result with a data-scope selection
     * @throws IllegalStateException when called on an unauthenticated/rejected result
     */
    public ThingifierApiScopedSessionResult useDataScope(final String dataScopeName) {
        return useDataScope(dataScopeName, DataScopeCreationPolicy.USE_EXISTING_ONLY);
    }

    /**
     * Selects a named data scope and missing-scope policy for the authenticated request.
     *
     * @param dataScopeName data scope chosen by trusted scoped-session resolver code
     * @param creationPolicy policy used when the scope does not exist
     * @return new result with a data-scope selection
     * @throws IllegalStateException when called on an unauthenticated/rejected result
     */
    public ThingifierApiScopedSessionResult useDataScope(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        requireAuthenticatedForDataScopeSelection();
        return withDataScopeSelection(
                ThingifierApiDataScopeSelection.named(dataScopeName, creationPolicy));
    }

    /**
     * Explicitly selects the model's default data scope for the authenticated request.
     *
     * <p>No data-scope selection preserves the current request context; this method deliberately
     * overrides any header/session-selected scope with the default scope.
     *
     * @return new result selecting the default data scope
     * @throws IllegalStateException when called on an unauthenticated/rejected result
     */
    public ThingifierApiScopedSessionResult useDefaultDataScope() {
        requireAuthenticatedForDataScopeSelection();
        return withDataScopeSelection(ThingifierApiDataScopeSelection.defaultDataScope());
    }

    /**
     * Reports whether the session credential was accepted.
     *
     * @return true when the principal may be trusted
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the application principal supplied by the resolver.
     *
     * @return principal object, or null when the resolver did not need one
     */
    public Object principal() {
        return principal;
    }

    /**
     * Returns the rejection response for an explicit scoped-session failure.
     *
     * @return API response to return, or null when default invalid handling should be used
     */
    public ApiResponse rejectionResponse() {
        return rejectionResponse;
    }

    /**
     * Reports whether the rejection response was supplied directly by application code.
     *
     * @return true when Thingifier should preserve the response exactly
     */
    public boolean hasCustomRejectionResponse() {
        return customRejectionResponse;
    }

    /**
     * Returns the data scope selected by trusted resolver code.
     *
     * @return selected data scope, or empty when existing request-context behaviour should remain
     */
    public Optional<ThingifierApiDataScopeSelection> dataScopeSelection() {
        return Optional.ofNullable(dataScopeSelection);
    }

    private ThingifierApiScopedSessionResult withDataScopeSelection(
            final ThingifierApiDataScopeSelection selection) {
        return new ThingifierApiScopedSessionResult(
                authenticated, principal, rejectionResponse, customRejectionResponse, selection);
    }

    private void requireAuthenticatedForDataScopeSelection() {
        if (!authenticated) {
            throw new IllegalStateException(
                    "data scopes can only be selected by authenticated scoped-session results");
        }
    }
}
