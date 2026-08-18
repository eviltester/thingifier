package uk.co.compendiumdev.thingifier.api.security;

/**
 * Authorizes an authenticated principal for one generated Thingifier API route.
 *
 * <p>Authenticators answer "who is this request?" while authorizers answer "may that principal do
 * this route-specific action?". Keeping these separate lets applications enforce domain rules such
 * as cart ownership without teaching Thingifier about the application domain.
 */
@FunctionalInterface
public interface ThingifierApiAuthorizer {

    /**
     * Authorizes the authenticated principal for the current request.
     *
     * @param context request, route, and principal details for the authorization decision
     * @return authorization result describing whether the request may continue
     */
    ThingifierApiAuthorizationResult authorize(ThingifierApiAuthorizationContext context);
}
