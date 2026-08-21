package uk.co.compendiumdev.thingifier.api.security;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Result returned by an application authenticator.
 *
 * <p>The result either supplies a principal for later authorization or an API response explaining
 * why the credentials cannot be accepted. Thingifier supplies the standard missing and malformed
 * bearer-token responses before invoking authenticators.
 *
 * <p>Successful authenticators may also choose a data scope for the rest of the request. That
 * selection is trusted because it is returned only after application code has validated the
 * credential.
 */
public final class ThingifierApiAuthenticationResult {

    private final boolean authenticated;
    private final Object principal;
    private final ApiResponse rejectionResponse;
    private final boolean customRejectionResponse;
    private final ThingifierApiDataScopeSelection dataScopeSelection;

    private ThingifierApiAuthenticationResult(
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
     * Creates a successful authentication result.
     *
     * @param principal application principal, token record, user id, or other caller-owned object
     * @return successful authentication result
     */
    public static ThingifierApiAuthenticationResult authenticated(final Object principal) {
        return new ThingifierApiAuthenticationResult(true, principal, null, false, null);
    }

    /**
     * Creates a successful authentication result without a principal object.
     *
     * @return successful authentication result
     */
    public static ThingifierApiAuthenticationResult authenticated() {
        return authenticated(null);
    }

    /**
     * Creates a 401 authentication rejection with a message.
     *
     * @param message message to render in the error body
     * @return rejected authentication result
     */
    public static ThingifierApiAuthenticationResult rejected(final String message) {
        return rejected(401, message);
    }

    /**
     * Creates an authentication rejection with a status and message.
     *
     * @param status status code to return
     * @param message message to render in the error body
     * @return rejected authentication result
     */
    public static ThingifierApiAuthenticationResult rejected(
            final int status, final String message) {
        return new ThingifierApiAuthenticationResult(
                false, null, ApiResponse.error(status, message), false, null);
    }

    /**
     * Creates an authentication rejection with a complete response.
     *
     * <p>Thingifier preserves custom rejection responses exactly. Use this when the application
     * needs full control over the response body and challenge headers, for example to suppress a
     * browser Basic auth dialog.
     *
     * @param response response to return instead of continuing request processing
     * @return rejected authentication result
     */
    public static ThingifierApiAuthenticationResult rejected(final ApiResponse response) {
        return new ThingifierApiAuthenticationResult(false, null, response, true, null);
    }

    /**
     * Selects a named data scope for the authenticated request.
     *
     * <p>The selected scope replaces any request-header/session-selected scope before authorizers,
     * validators, handlers, and response rendering continue. Missing scopes are not created unless
     * a creation policy is supplied through {@link #useDataScope(String, DataScopeCreationPolicy)}.
     *
     * @param dataScopeName data scope chosen by trusted authentication code
     * @return new authentication result with a data-scope selection
     * @throws IllegalStateException when called on a rejected authentication result
     */
    public ThingifierApiAuthenticationResult useDataScope(final String dataScopeName) {
        return useDataScope(dataScopeName, DataScopeCreationPolicy.USE_EXISTING_ONLY);
    }

    /**
     * Selects a named data scope and missing-scope policy for the authenticated request.
     *
     * @param dataScopeName data scope chosen by trusted authentication code
     * @param creationPolicy policy used when the scope does not exist
     * @return new authentication result with a data-scope selection
     * @throws IllegalStateException when called on a rejected authentication result
     */
    public ThingifierApiAuthenticationResult useDataScope(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        requireAuthenticatedForDataScopeSelection();
        return withDataScopeSelection(
                ThingifierApiDataScopeSelection.named(dataScopeName, creationPolicy));
    }

    /**
     * Explicitly selects the model's default data scope for the authenticated request.
     *
     * <p>This is different from returning no data-scope selection. No selection preserves existing
     * request context behaviour, while this method intentionally overrides any session/header
     * selected scope.
     *
     * @return new authentication result selecting the default data scope
     * @throws IllegalStateException when called on a rejected authentication result
     */
    public ThingifierApiAuthenticationResult useDefaultDataScope() {
        requireAuthenticatedForDataScopeSelection();
        return withDataScopeSelection(ThingifierApiDataScopeSelection.defaultDataScope());
    }

    /**
     * Reports whether the request was authenticated.
     *
     * @return true when a principal may be trusted by authorizers
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the application principal supplied by the authenticator.
     *
     * @return principal object, or null when the authenticator did not need one
     */
    public Object principal() {
        return principal;
    }

    /**
     * Returns the rejection response for a failed authentication.
     *
     * @return API response to return, or null when authentication succeeded
     */
    public ApiResponse rejectionResponse() {
        return rejectionResponse;
    }

    /**
     * Reports whether the rejection response was supplied directly by application code.
     *
     * @return true when Thingifier should not add default challenge headers
     */
    public boolean hasCustomRejectionResponse() {
        return customRejectionResponse;
    }

    /**
     * Returns the data scope selected by trusted authentication code.
     *
     * @return selected data scope, or empty when current request-context behaviour should remain
     */
    public Optional<ThingifierApiDataScopeSelection> dataScopeSelection() {
        return Optional.ofNullable(dataScopeSelection);
    }

    private ThingifierApiAuthenticationResult withDataScopeSelection(
            final ThingifierApiDataScopeSelection selection) {
        return new ThingifierApiAuthenticationResult(
                authenticated, principal, rejectionResponse, customRejectionResponse, selection);
    }

    private void requireAuthenticatedForDataScopeSelection() {
        if (!authenticated) {
            throw new IllegalStateException(
                    "data scopes can only be selected by authenticated results");
        }
    }
}
