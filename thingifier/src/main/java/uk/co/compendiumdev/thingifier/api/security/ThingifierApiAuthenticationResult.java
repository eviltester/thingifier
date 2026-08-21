package uk.co.compendiumdev.thingifier.api.security;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Result returned by an application authenticator.
 *
 * <p>The result either supplies a principal for later authorization or an API response explaining
 * why the credentials cannot be accepted. Thingifier supplies the standard missing and malformed
 * bearer-token responses before invoking authenticators.
 */
public final class ThingifierApiAuthenticationResult {

    private final boolean authenticated;
    private final Object principal;
    private final ApiResponse rejectionResponse;
    private final boolean customRejectionResponse;

    private ThingifierApiAuthenticationResult(
            final boolean authenticated,
            final Object principal,
            final ApiResponse rejectionResponse,
            final boolean customRejectionResponse) {
        this.authenticated = authenticated;
        this.principal = principal;
        this.rejectionResponse = rejectionResponse;
        this.customRejectionResponse = customRejectionResponse;
    }

    /**
     * Creates a successful authentication result.
     *
     * @param principal application principal, token record, user id, or other caller-owned object
     * @return successful authentication result
     */
    public static ThingifierApiAuthenticationResult authenticated(final Object principal) {
        return new ThingifierApiAuthenticationResult(true, principal, null, false);
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
                false, null, ApiResponse.error(status, message), false);
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
        return new ThingifierApiAuthenticationResult(false, null, response, true);
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
}
