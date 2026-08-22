package uk.co.compendiumdev.thingifier.api.security;

/**
 * Identifies how a named security scheme supplies credentials on an HTTP request.
 *
 * <p>Route rules can accept security scheme names without repeating transport details. The API
 * security declaration resolves each name to one of these types so runtime authentication and
 * OpenAPI generation both read the same credential source.
 */
public enum ThingifierApiSecuritySchemeType {
    /** HTTP Basic credentials from the {@code Authorization} header. */
    BASIC,

    /** HTTP Bearer token from the {@code Authorization} header. */
    BEARER,

    /** Opaque API key from a configured request header. */
    API_KEY
}
