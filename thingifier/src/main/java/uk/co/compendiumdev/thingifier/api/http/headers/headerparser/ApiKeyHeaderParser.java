package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

/**
 * Parses an API key credential from a configured request header.
 *
 * <p>API key auth treats the header name as part of the public API contract and the value as an
 * opaque credential. Thingifier only checks that a non-blank credential is present before handing
 * it to application authenticator code.
 */
public final class ApiKeyHeaderParser {

    private final String credential;

    /**
     * Creates a parser for one configured API key header value.
     *
     * @param headerValue raw header value from the request
     */
    public ApiKeyHeaderParser(final String headerValue) {
        credential = headerValue == null ? "" : headerValue.trim();
    }

    /**
     * Reports whether the configured header supplied a usable credential.
     *
     * @return true when the header value is present and not blank
     */
    public boolean isValid() {
        return !credential.isEmpty();
    }

    /**
     * Returns the parsed API key credential.
     *
     * @return trimmed API key value, or an empty string when invalid
     */
    public String credential() {
        return credential;
    }
}
