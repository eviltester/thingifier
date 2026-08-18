package uk.co.compendiumdev.thingifier.api.security;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Result returned by an application authorizer.
 *
 * <p>Authorizers can allow a request, return the standard forbidden response, or provide a
 * route/domain-specific response such as 404 when the protected parent resource does not exist.
 */
public final class ThingifierApiAuthorizationResult {

    private final boolean authorized;
    private final ApiResponse rejectionResponse;

    private ThingifierApiAuthorizationResult(
            final boolean authorized, final ApiResponse rejectionResponse) {
        this.authorized = authorized;
        this.rejectionResponse = rejectionResponse;
    }

    /**
     * Creates a successful authorization result.
     *
     * @return successful authorization result
     */
    public static ThingifierApiAuthorizationResult authorized() {
        return new ThingifierApiAuthorizationResult(true, null);
    }

    /**
     * Creates a 403 Forbidden authorization rejection.
     *
     * @return forbidden authorization result
     */
    public static ThingifierApiAuthorizationResult forbidden() {
        return rejected(403, "Forbidden");
    }

    /**
     * Creates an authorization rejection with a status and message.
     *
     * @param status status code to return
     * @param message message to render in the error body
     * @return rejected authorization result
     */
    public static ThingifierApiAuthorizationResult rejected(
            final int status, final String message) {
        return rejected(ApiResponse.error(status, message));
    }

    /**
     * Creates an authorization rejection with a complete response.
     *
     * @param response response to return instead of continuing request processing
     * @return rejected authorization result
     */
    public static ThingifierApiAuthorizationResult rejected(final ApiResponse response) {
        return new ThingifierApiAuthorizationResult(false, response);
    }

    /**
     * Reports whether the authenticated principal may use the route.
     *
     * @return true when request processing may continue
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Returns the response for a failed authorization.
     *
     * @return API response to return, or null when authorization succeeded
     */
    public ApiResponse rejectionResponse() {
        return rejectionResponse;
    }
}
