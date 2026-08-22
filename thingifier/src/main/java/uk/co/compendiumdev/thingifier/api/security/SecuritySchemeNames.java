package uk.co.compendiumdev.thingifier.api.security;

/**
 * Normalizes and validates public Thingifier API security scheme names.
 *
 * <p>OpenAPI security scheme names are map keys, so accepting blank names would create confusing
 * documentation and runtime lookup behaviour.
 */
public final class SecuritySchemeNames {

    /** Default OpenAPI scheme name used by the historical no-argument bearer marker. */
    public static final String DEFAULT_BEARER_AUTH_SCHEME = "bearerAuth";

    /** Default OpenAPI scheme name used by the historical no-argument Basic marker. */
    public static final String DEFAULT_BASIC_AUTH_SCHEME = "basicAuth";

    /** Default OpenAPI scheme name used by API key routes when no explicit name is supplied. */
    public static final String DEFAULT_API_KEY_AUTH_SCHEME = "apiKeyAuth";

    private SecuritySchemeNames() {}

    /**
     * Validates and trims a security scheme name.
     *
     * @param schemeName caller supplied scheme name
     * @return trimmed scheme name
     * @throws IllegalArgumentException when the scheme name is null or blank
     */
    public static String requireValid(final String schemeName) {
        if (schemeName == null || schemeName.trim().isEmpty()) {
            throw new IllegalArgumentException("security scheme name is required");
        }
        return schemeName.trim();
    }

    /**
     * Validates and trims an HTTP header name used as an auth credential source.
     *
     * <p>API key schemes expose their header names in OpenAPI and use them for runtime credential
     * lookup, so rejecting blank or malformed names prevents confusing docs and unsafe response
     * metadata.
     *
     * @param headerName caller supplied HTTP header name
     * @return trimmed header name
     * @throws IllegalArgumentException when the header name is null, blank, or not an HTTP token
     */
    public static String requireValidHeaderName(final String headerName) {
        if (headerName == null || headerName.trim().isEmpty()) {
            throw new IllegalArgumentException("API key header name is required");
        }
        final String normalized = headerName.trim();
        if (!normalized.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) {
            throw new IllegalArgumentException("API key header name must be an HTTP field name");
        }
        return normalized;
    }
}
