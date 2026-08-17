package uk.co.compendiumdev.thingifier.api.security;

/**
 * Authenticates credentials supplied to a protected Thingifier API route.
 *
 * <p>Thingifier owns the generic HTTP bearer flow and calls this interface only after a bearer
 * token has been found in the {@code Authorization} header. Applications keep their token lookup,
 * expiry, and principal creation logic outside the library by implementing this callback.
 */
@FunctionalInterface
public interface ThingifierApiAuthenticator {

    /**
     * Authenticates the bearer token for the configured security scheme.
     *
     * @param context request and route details for the authentication decision
     * @return authentication result describing the authenticated principal or rejection response
     */
    ThingifierApiAuthenticationResult authenticate(ThingifierApiAuthenticationContext context);
}
