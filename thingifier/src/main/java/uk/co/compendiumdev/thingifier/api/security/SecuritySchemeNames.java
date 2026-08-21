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
}
